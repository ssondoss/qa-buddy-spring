package com.sondos.qa_buddy.workflows.versions.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.versions.client.RecordingNodeClient;
import com.sondos.qa_buddy.workflows.versions.client.RecordingStartNodeRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.versions.dto.StepRequest;
import com.sondos.qa_buddy.workflows.versions.dto.RecordingCompleteRequest;
import com.sondos.qa_buddy.workflows.versions.dto.VersionView;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import com.sondos.qa_buddy.workflows.environments.service.EnvironmentService;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.factory.WorkflowResponseFactory;
import com.sondos.qa_buddy.workflows.versions.exception.VersionNotFoundException;
import com.sondos.qa_buddy.workflows.versions.factory.StepFactory;
import com.sondos.qa_buddy.workflows.versions.factory.VersionResponseFactory;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VersionService {
	private final CurrentUserService currentUserService;
	private final StepFactory stepFactory;
	private final WorkflowRepository workflowRepository;
	private final VersionRepository versionRepository;
	private final StepRepository stepRepository;
	private final EnvironmentService environmentService;
	private final RecordingNodeClient recordingNodeClient;
	private final WorkflowResponseFactory workflowResponseFactory;
	private final VersionResponseFactory versionResponseFactory;

	public WorkflowResponse startRecording(Jwt jwt, UUID workflowId, com.sondos.qa_buddy.workflows.versions.dto.RecordingStartRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Environment environment = environmentService.getRequiredEnvironment(user.getId(), request.environmentId());
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		Instant now = Instant.now();
		int nextVersionNumber = versionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId)
				.map(Version::getVersionNumber)
				.orElse(0) + 1;
		Version version = createVersion(workflow, user, now, nextVersionNumber, VersionStatus.RECORDING, environment);
		workflow.updateDetails(workflow.getName(), workflow.getDescription(), now);
		workflowRepository.save(workflow);

		try {
			recordingNodeClient.startRecording(new RecordingStartNodeRequest(
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
			version.updateStatus(VersionStatus.FAILED, Instant.now());
			versionRepository.save(version);
			throw ex;
		}

		return workflowResponseFactory.toResponse(
				workflow,
				Math.toIntExact(versionRepository.countByWorkflowId(workflowId))
		);
	}

	@Transactional
	public WorkflowResponse completeRecording(UUID workflowVersionId, RecordingCompleteRequest request) {
		Version version = versionRepository.findById(workflowVersionId)
				.orElseThrow(VersionNotFoundException::new);
		Workflow workflow = version.getWorkflow();
		Instant now = Instant.now();

		stepRepository.deleteByVersionId(version.getId());

		List<Step> steps = buildSteps(version, version.getCreatedBy(), now, request.steps());
		stepRepository.saveAll(steps);

		VersionStatus finalStatus = request.status() == null ? VersionStatus.READY : request.status();
		version.updateStatus(finalStatus, now);
		versionRepository.save(version);

		workflow.updateDetails(workflow.getName(), workflow.getDescription(), now);
		workflowRepository.save(workflow);

		return workflowResponseFactory.toResponse(
				workflow,
				Math.toIntExact(versionRepository.countByWorkflowId(workflow.getId()))
		);
	}

	@Transactional(readOnly = true)
	public List<VersionView> getByWorkflowId(UUID workflowId) {
		workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		return versionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflowId)
				.stream()
				.map(versionResponseFactory::toView)
				.toList();
	}

	private Version createVersion(
			Workflow workflow,
			User user,
			Instant now,
			int versionNumber,
			VersionStatus status,
			Environment environment
	) {
		Version version = Version.builder()
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

		return versionRepository.save(version);
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

	private List<Step> buildSteps(
			Version version,
			User user,
			Instant now,
			List<StepRequest> steps
	) {
		List<StepRequest> safeSteps = steps == null ? List.of() : steps;

		return IntStream.range(0, safeSteps.size())
				.mapToObj(index -> stepFactory.create(
						version,
						user,
						now,
						index + 1,
						safeSteps.get(index),
						StepSource.RECORDED
				))
				.toList();
	}
}
