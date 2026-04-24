package com.sondos.qa_buddy.workflows.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.shared.api.PaginatedResponse;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowStatus;
import com.sondos.qa_buddy.workflows.factory.WorkflowStepFactory;
import com.sondos.qa_buddy.workflows.factory.WorkflowResponseFactory;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowStepRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkflowService {
	private final CurrentUserService currentUserService;
	private final WorkflowStepFactory workflowStepFactory;
	private final WorkflowResponseFactory workflowResponseFactory;
	private final WorkflowRepository workflowRepository;
	private final WorkflowVersionRepository workflowVersionRepository;
	private final WorkflowStepRepository workflowStepRepository;

	public WorkflowResponse create(Jwt jwt, WorkflowRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Instant now = Instant.now();

		Workflow savedWorkflow = workflowRepository.save(Workflow.builder()
				.id(UUID.randomUUID())
				.name(request.name())
				.description(request.description())
				.status(request.status() == null ? WorkflowStatus.DRAFT : request.status())
				.currentVersion(1)
				.createdBy(user)
				.createdAt(now)
				.updatedAt(now)
				.build());

		WorkflowVersion savedVersion = workflowVersionRepository.save(WorkflowVersion.builder()
				.id(UUID.randomUUID())
				.workflow(savedWorkflow)
				.versionNumber(1)
				.createdBy(user)
				.createdAt(now)
				.build());

		List<WorkflowStep> steps = buildSteps(savedVersion, user, now, request);
		workflowStepRepository.saveAll(steps);

		return new WorkflowResponse(
				savedWorkflow.getId(),
				savedVersion.getId(),
				savedVersion.getVersionNumber(),
				steps.size(),
				savedWorkflow.getStatus()
		);
	}

	public WorkflowResponse update(Jwt jwt, UUID workflowId, WorkflowRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		Instant now = Instant.now();
		int nextVersionNumber = workflow.getCurrentVersion() + 1;

		WorkflowVersion savedVersion = workflowVersionRepository.save(WorkflowVersion.builder()
				.id(UUID.randomUUID())
				.workflow(workflow)
				.versionNumber(nextVersionNumber)
				.createdBy(user)
				.createdAt(now)
				.build());

		List<WorkflowStep> steps = buildSteps(savedVersion, user, now, request);
		workflowStepRepository.saveAll(steps);

		workflow.updateDetails(
				request.name(),
				request.description(),
				request.status() == null ? workflow.getStatus() : request.status(),
				nextVersionNumber,
				now
		);
		workflowRepository.save(workflow);

		return new WorkflowResponse(
				workflow.getId(),
				savedVersion.getId(),
				savedVersion.getVersionNumber(),
				steps.size(),
				workflow.getStatus()
		);
	}

	@Transactional(readOnly = true)
	public PaginatedResponse<WorkflowListItemResponse> getAll(Pageable pageable) {
		return workflowResponseFactory.toPage(workflowRepository.findByDeletedAtIsNull(pageable));
	}

	@Transactional(readOnly = true)
	public WorkflowDetailResponse getById(UUID workflowId) {
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		WorkflowVersion version = workflowVersionRepository.findByWorkflowIdAndVersionNumber(
						workflowId,
						workflow.getCurrentVersion()
				)
				.orElseThrow(WorkflowNotFoundException::new);

		List<WorkflowStep> steps = workflowStepRepository.findByWorkflowVersionIdOrderByOrderIndexAsc(version.getId());
		return workflowResponseFactory.toDetail(workflow, version, steps);
	}

	private List<WorkflowStep> buildSteps(
			WorkflowVersion version,
			User user,
			Instant now,
			WorkflowRequest request
	) {
		List<com.sondos.qa_buddy.workflows.dto.WorkflowStepRequest> steps = request.steps() == null
				? List.of()
				: request.steps();

		return IntStream.range(0, steps.size())
				.mapToObj(index -> workflowStepFactory.create(
						version,
						user,
						now,
						index + 1,
						steps.get(index)
				))
				.toList();
	}
}
