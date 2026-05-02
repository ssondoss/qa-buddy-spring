package com.sondos.qa_buddy.workflows.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowUpsertRequest;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.factory.WorkflowResponseFactory;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
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
	private final VersionRepository versionRepository;

	public WorkflowResponse create(Jwt jwt, WorkflowUpsertRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Instant now = Instant.now();

		Workflow savedWorkflow = workflowRepository.save(Workflow.builder()
				.id(UUID.randomUUID())
				.name(request.name())
				.description(request.description())
				.createdBy(user)
				.createdAt(now)
				.updatedAt(now)
				.build());

		return workflowResponseFactory.toResponse(savedWorkflow, 0);
	}

	public WorkflowResponse update(Jwt jwt, UUID workflowId, WorkflowUpsertRequest request) {
		currentUserService.getCurrentUser(jwt);
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);

		Instant now = Instant.now();
		workflow.updateDetails(
				request.name(),
				request.description(),
				now
		);
		workflowRepository.save(workflow);

		int versionCount = Math.toIntExact(versionRepository.countByWorkflowId(workflow.getId()));
		return workflowResponseFactory.toResponse(workflow, versionCount);
	}

	@Transactional(readOnly = true)
	public List<WorkflowResponse> getAll() {
		return workflowResponseFactory.toList(
				workflowRepository.findByDeletedAtIsNullOrderByUpdatedAtDesc(),
				versionRepository
		);
	}
}
