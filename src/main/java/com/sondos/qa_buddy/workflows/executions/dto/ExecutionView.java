package com.sondos.qa_buddy.workflows.executions.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStatus;
import org.jspecify.annotations.Nullable;

public record ExecutionView(
		UUID id,
		UUID workflowId,
		UUID versionId,
		int versionNumber,
		ExecutionStatus status,
		UUID triggeredById,
		String environmentNameSnapshot,
		String environmentWebBaseUrlSnapshot,
		@Nullable String environmentApiBaseUrlSnapshot,
		Instant startedAt,
		@Nullable Instant finishedAt,
		List<ExecutionStepResultView> stepResults
) {
	public ExecutionView {
		stepResults = stepResults == null ? List.of() : List.copyOf(stepResults);
	}
}
