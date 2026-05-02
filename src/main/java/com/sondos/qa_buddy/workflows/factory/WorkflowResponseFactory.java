package com.sondos.qa_buddy.workflows.factory;

import java.util.LinkedHashMap;
import java.util.List;

import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.versions.dto.StepView;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkflowResponseFactory {
	public WorkflowResponse toResponse(Workflow workflow, int versionCount) {
		return new WorkflowResponse(
				workflow.getId(),
				workflow.getName(),
				workflow.getDescription(),
				versionCount,
				workflow.getCreatedAt(),
				workflow.getUpdatedAt()
		);
	}

	public StepView toStepView(Step step) {
		return new StepView(
				step.getId(),
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

	public List<WorkflowResponse> toList(
			List<Workflow> workflows,
			VersionRepository versionRepository
	) {
		return workflows.stream()
				.map(workflow -> toResponse(
						workflow,
						Math.toIntExact(versionRepository.countByWorkflowId(workflow.getId()))
				))
				.toList();
	}
}
