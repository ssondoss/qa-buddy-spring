package com.sondos.qa_buddy.workflows.factory;

import java.util.LinkedHashMap;
import java.util.List;

import com.sondos.qa_buddy.shared.api.PaginatedResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowStepView;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class WorkflowResponseFactory {
	public WorkflowListItemResponse toListItem(Workflow workflow) {
		return new WorkflowListItemResponse(
				workflow.getId(),
				workflow.getName(),
				workflow.getDescription(),
				workflow.getStatus(),
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
				workflow.getStatus(),
				workflow.getCreatedAt(),
				workflow.getUpdatedAt(),
				steps.stream().map(this::toStepView).toList()
		);
	}

	public PaginatedResponse<WorkflowListItemResponse> toPage(Page<Workflow> page) {
		return new PaginatedResponse<>(
				page.getContent().stream().map(this::toListItem).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast()
		);
	}
}
