package com.sondos.qa_buddy.workflows.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.client.RecordingNodeClient;
import com.sondos.qa_buddy.workflows.client.RecordingStartRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowRecordingStartRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowStepRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowVersionCompleteRequest;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;
import com.sondos.qa_buddy.workflows.enums.WorkflowVersionStatus;
import com.sondos.qa_buddy.workflows.environments.service.EnvironmentService;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.exception.WorkflowVersionNotFoundException;
import com.sondos.qa_buddy.workflows.factory.WorkflowStepFactory;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowStepRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowVersionService {
	private final CurrentUserService currentUserService;
	private final WorkflowStepFactory workflowStepFactory;
	private final WorkflowRepository workflowRepository;
	private final WorkflowVersionRepository workflowVersionRepository;
	private final WorkflowStepRepository workflowStepRepository;
	private final EnvironmentService environmentService;
	private final RecordingNodeClient recordingNodeClient;

	public WorkflowResponse startRecording(Jwt jwt, UUID workflowId, WorkflowRecordingStartRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Environment environment = environmentService.getRequiredEnvironment(user.getId(), request.environmentId());
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		Instant now = Instant.now();
		WorkflowVersion version = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflowId)
				.stream()
				.findFirst()
				.orElse(null);

		if (version == null) {
			version = createVersion(workflow, user, now, 1, WorkflowVersionStatus.RECORDING, environment);
			workflow.updateDetails(workflow.getName(), workflow.getDescription(), 1, now);
			workflowRepository.save(workflow);
		} else if (version.getStatus() == WorkflowVersionStatus.DRAFT) {
			version.beginRecording(
					environment.getName(),
					environment.getWebBaseUrl(),
					environment.getApiBaseUrl(),
					now
			);
			workflowVersionRepository.save(version);
			workflow.updateDetails(workflow.getName(), workflow.getDescription(), workflow.getCurrentVersion(), now);
			workflowRepository.save(workflow);
		} else if (version.getStatus() == WorkflowVersionStatus.RECORDING) {
			return toResponse(workflow, version, 0);
		} else {
			int nextVersionNumber = workflow.getCurrentVersion() + 1;
			version = createVersion(workflow, user, now, nextVersionNumber, WorkflowVersionStatus.RECORDING, environment);
			workflow.updateDetails(workflow.getName(), workflow.getDescription(), nextVersionNumber, now);
			workflowRepository.save(workflow);
		}

		try {
			recordingNodeClient.startRecording(new RecordingStartRequest(
					workflow.getId(),
					version.getId(),
					version.getVersionNumber(),
					workflow.getName(),
					workflow.getDescription(),
					normalizeBaseUrl(environment.getWebBaseUrl()),
					normalizeBaseUrl(environment.getApiBaseUrl()),
					normalizeStartPath(request.startPath())
			));
		} catch (RuntimeException ex) {
			version.updateStatus(WorkflowVersionStatus.FAILED, Instant.now());
			workflowVersionRepository.save(version);
			throw ex;
		}

		return toResponse(workflow, version, 0);
	}

	@Transactional
	public WorkflowResponse completeRecording(UUID workflowVersionId, WorkflowVersionCompleteRequest request) {
		WorkflowVersion version = workflowVersionRepository.findById(workflowVersionId)
				.orElseThrow(WorkflowVersionNotFoundException::new);
		Workflow workflow = version.getWorkflow();
		Instant now = Instant.now();

		workflowStepRepository.deleteByWorkflowVersionId(version.getId());

		List<WorkflowStep> steps = buildSteps(version, version.getCreatedBy(), now, request.steps());
		workflowStepRepository.saveAll(steps);

		WorkflowVersionStatus finalStatus = request.status() == null ? WorkflowVersionStatus.READY : request.status();
		version.updateStatus(finalStatus, now);
		workflowVersionRepository.save(version);

		int currentVersion = Math.max(workflow.getCurrentVersion(), version.getVersionNumber());
		workflow.updateDetails(workflow.getName(), workflow.getDescription(), currentVersion, now);
		workflowRepository.save(workflow);

		return toResponse(workflow, version, steps.size());
	}

	private WorkflowVersion createVersion(
			Workflow workflow,
			User user,
			Instant now,
			int versionNumber,
			WorkflowVersionStatus status,
			Environment environment
	) {
		WorkflowVersion version = WorkflowVersion.builder()
				.id(UUID.randomUUID())
				.workflow(workflow)
				.versionNumber(versionNumber)
				.status(status)
				.createdBy(user)
				.createdAt(now)
				.updatedAt(now)
				.build();

		if (environment != null) {
			version.captureEnvironmentSnapshot(
					environment.getName(),
					environment.getWebBaseUrl(),
					environment.getApiBaseUrl()
			);
		}

		return workflowVersionRepository.save(version);
	}

	private WorkflowResponse toResponse(Workflow workflow, WorkflowVersion version, int stepCount) {
		return new WorkflowResponse(
				workflow.getId(),
				version.getId(),
				version.getVersionNumber(),
				stepCount,
				version.getStatus()
		);
	}

	private String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}

		String normalized = baseUrl.trim();
		if (normalized.isEmpty()) {
			return null;
		}

		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	private String normalizeStartPath(String startPath) {
		if (startPath == null) {
			return null;
		}

		String normalized = startPath.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private List<WorkflowStep> buildSteps(
			WorkflowVersion version,
			User user,
			Instant now,
			List<WorkflowStepRequest> steps
	) {
		List<WorkflowStepRequest> safeSteps = steps == null ? List.of() : steps;

		return IntStream.range(0, safeSteps.size())
				.mapToObj(index -> workflowStepFactory.create(
						version,
						user,
						now,
						index + 1,
						safeSteps.get(index),
						WorkflowStepSource.RECORDED
				))
				.toList();
	}
}
