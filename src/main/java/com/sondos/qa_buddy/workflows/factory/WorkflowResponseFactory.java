package com.sondos.qa_buddy.workflows.factory;

import java.util.LinkedHashMap;
import java.util.List;

import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.versions.dto.StepView;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkflowResponseFactory {
	public WorkflowListItemResponse toListItem(Workflow workflow, Version version) {
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

	public StepView toStepView(Step step) {
		return new StepView(
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

	public WorkflowDetailResponse toDetail(Workflow workflow, Version version, List<Step> steps) {
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
			VersionRepository versionRepository
	) {
		return workflows.stream()
				.map(workflow -> versionRepository.findByWorkflowIdAndVersionNumber(
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
