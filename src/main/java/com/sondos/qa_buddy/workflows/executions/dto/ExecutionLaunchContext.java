package com.sondos.qa_buddy.workflows.executions.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ExecutionLaunchContext(
		UUID userId,
		UUID workflowId,
		String workflowName,
		@Nullable String workflowDescription,
		UUID versionId,
		int versionNumber,
		String environmentNameSnapshot,
		String environmentWebBaseUrlSnapshot,
		@Nullable String environmentApiBaseUrlSnapshot,
		List<ExecutionStepRequest> steps
) {
	public ExecutionLaunchContext {
		userId = Objects.requireNonNull(userId, "userId must not be null");
		workflowId = Objects.requireNonNull(workflowId, "workflowId must not be null");
		workflowName = Objects.requireNonNull(workflowName, "workflowName must not be null");
		versionId = Objects.requireNonNull(versionId, "versionId must not be null");
		environmentNameSnapshot = Objects.requireNonNull(
				environmentNameSnapshot,
				"environmentNameSnapshot must not be null"
		);
		environmentWebBaseUrlSnapshot = Objects.requireNonNull(
				environmentWebBaseUrlSnapshot,
				"environmentWebBaseUrlSnapshot must not be null"
		);
		steps = steps == null ? List.of() : List.copyOf(steps);
	}
}
