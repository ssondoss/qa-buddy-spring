package com.sondos.qa_buddy.workflows.factory;

import java.util.LinkedHashMap;
import java.util.List;

import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowStepView;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkflowResponseFactory {
	public WorkflowListItemResponse toListItem(Workflow workflow, WorkflowVersion version) {
		return new WorkflowListItemResponse(
				workflow.getId(),
				workflow.getName(),
				workflow.getDescription(),
				version.getStatus(),
				workflow.getCurrentVersion(),
				workflow.getCreatedAt(),
				workflow.getUpdatedAt()
		);
	}

	public WorkflowStepView toStepView(WorkflowStep step) {
		return new WorkflowStepView(
				step.getOrderIndex(),
				step.getAction(),
				step.getSource(),
				step.getSelector(),
				step.getValue(),
				step.getExpectedValue(),
				step.getMeta() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(step.getMeta()),
				step.isOptional(),
				step.getTimeoutMs(),
				step.getRetryCount()
		);
	}

	public WorkflowDetailResponse toDetail(Workflow workflow, WorkflowVersion version, List<WorkflowStep> steps) {
		return new WorkflowDetailResponse(
				workflow.getId(),
				version.getId(),
				version.getVersionNumber(),
				workflow.getName(),
				workflow.getDescription(),
				version.getStatus(),
				workflow.getCreatedAt(),
				workflow.getUpdatedAt(),
				steps.stream().map(this::toStepView).toList()
		);
	}

	public List<WorkflowListItemResponse> toList(
			List<Workflow> workflows,
			WorkflowVersionRepository workflowVersionRepository
	) {
		return workflows.stream()
				.map(workflow -> workflowVersionRepository.findByWorkflowIdAndVersionNumber(
								workflow.getId(),
								workflow.getCurrentVersion()
						)
						.map(version -> toListItem(workflow, version))
						.orElseGet(() -> new WorkflowListItemResponse(
								workflow.getId(),
								workflow.getName(),
								workflow.getDescription(),
								null,
								workflow.getCurrentVersion(),
								workflow.getCreatedAt(),
								workflow.getUpdatedAt()
						)))
				.toList();
	}
}
