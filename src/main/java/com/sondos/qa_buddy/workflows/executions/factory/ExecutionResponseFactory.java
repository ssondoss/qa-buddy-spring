package com.sondos.qa_buddy.workflows.executions.factory;

import java.util.LinkedHashMap;
import java.util.List;

import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStepResultView;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionView;
import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import com.sondos.qa_buddy.workflows.executions.entity.ExecutionStepResult;
import org.springframework.stereotype.Component;

@Component
public class ExecutionResponseFactory {
	public ExecutionView toView(Execution execution, List<ExecutionStepResult> stepResults) {
		return new ExecutionView(
				execution.getId(),
				execution.getWorkflow().getId(),
				execution.getVersion().getId(),
				execution.getVersion().getVersionNumber(),
				execution.getStatus(),
				execution.getTriggeredBy().getId(),
				execution.getEnvironmentNameSnapshot(),
				execution.getEnvironmentWebBaseUrlSnapshot(),
				execution.getEnvironmentApiBaseUrlSnapshot(),
				execution.getStartedAt(),
				execution.getFinishedAt(),
				stepResults == null ? List.of() : stepResults.stream()
						.map(this::toStepResultView)
						.toList()
		);
	}

	public ExecutionStepResultView toStepResultView(ExecutionStepResult stepResult) {
		return new ExecutionStepResultView(
				stepResult.getStep().getId(),
				stepResult.getOrderIndex(),
				stepResult.getStatus(),
				stepResult.getErrorMessage(),
				stepResult.getScreenshotUrl(),
				stepResult.getLog(),
				stepResult.getStartedAt(),
				stepResult.getFinishedAt(),
				stepResult.getDurationMs(),
				stepResult.getStepSnapshot() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(stepResult.getStepSnapshot())
		);
	}
}
