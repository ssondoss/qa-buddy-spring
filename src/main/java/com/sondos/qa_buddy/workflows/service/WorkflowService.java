package com.sondos.qa_buddy.workflows.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowVersionStatus;
import com.sondos.qa_buddy.workflows.factory.WorkflowResponseFactory;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowStepRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkflowService {
	private final CurrentUserService currentUserService;
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
				.currentVersion(1)
				.createdBy(user)
				.createdAt(now)
				.updatedAt(now)
				.build());

		WorkflowVersion savedVersion = workflowVersionRepository.save(
				WorkflowVersion.builder()
						.id(UUID.randomUUID())
						.workflow(savedWorkflow)
						.versionNumber(1)
						.status(WorkflowVersionStatus.DRAFT)
						.createdBy(user)
						.createdAt(now)
						.updatedAt(now)
						.build()
		);

		return new WorkflowResponse(
				savedWorkflow.getId(),
				savedVersion.getId(),
				savedVersion.getVersionNumber(),
				0,
				savedVersion.getStatus()
		);
	}

	public WorkflowResponse update(Jwt jwt, UUID workflowId, WorkflowRequest request) {
		currentUserService.getCurrentUser(jwt);
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		Instant now = Instant.now();
		workflow.updateDetails(
				request.name(),
				request.description(),
				workflow.getCurrentVersion(),
				now
		);
		workflowRepository.save(workflow);

		WorkflowVersion currentVersion = workflowVersionRepository.findByWorkflowIdAndVersionNumber(
						workflow.getId(),
						workflow.getCurrentVersion()
				)
				.orElseThrow(WorkflowNotFoundException::new);

		return new WorkflowResponse(
				workflow.getId(),
				currentVersion.getId(),
				currentVersion.getVersionNumber(),
				0,
				currentVersion.getStatus()
		);
	}

	@Transactional(readOnly = true)
	public List<WorkflowListItemResponse> getAll() {
		return workflowResponseFactory.toList(
				workflowRepository.findByDeletedAtIsNullOrderByUpdatedAtDesc(),
				workflowVersionRepository
		);
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
}
