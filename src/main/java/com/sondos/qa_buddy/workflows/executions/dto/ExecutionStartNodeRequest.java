package com.sondos.qa_buddy.workflows.executions.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ExecutionStartNodeRequest(
		UUID executionId,
		UUID workflowId,
		UUID workflowVersionId,
		int versionNumber,
		String workflowName,
		@Nullable String workflowDescription,
		String environmentName,
		String webBaseUrl,
		@Nullable String apiBaseUrl,
		List<ExecutionStepRequest> steps
) {
	public ExecutionStartNodeRequest {
		executionId = Objects.requireNonNull(executionId, "executionId must not be null");
		workflowId = Objects.requireNonNull(workflowId, "workflowId must not be null");
		workflowVersionId = Objects.requireNonNull(workflowVersionId, "workflowVersionId must not be null");
		workflowName = Objects.requireNonNull(workflowName, "workflowName must not be null");
		environmentName = Objects.requireNonNull(environmentName, "environmentName must not be null");
		webBaseUrl = Objects.requireNonNull(webBaseUrl, "webBaseUrl must not be null");
		steps = steps == null ? List.of() : List.copyOf(steps);
	}
}
