package com.sondos.qa_buddy.workflows.executions.dto;

import java.time.Instant;
import java.util.List;

import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStatus;
import org.jspecify.annotations.Nullable;

public record ExecutionRunNodeResponse(
		@Nullable ExecutionStatus status,
		@Nullable Instant startedAt,
		@Nullable Instant finishedAt,
		@Nullable String errorMessage,
		@Nullable String log,
		List<ExecutionStepRunResponse> stepResults
) {
	public ExecutionRunNodeResponse {
		stepResults = stepResults == null ? List.of() : List.copyOf(stepResults);
	}
}
