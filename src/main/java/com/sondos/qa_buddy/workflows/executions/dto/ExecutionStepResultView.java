package com.sondos.qa_buddy.workflows.executions.dto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStepStatus;
import org.jspecify.annotations.Nullable;

public record ExecutionStepResultView(
		UUID stepId,
		int orderIndex,
		ExecutionStepStatus status,
		@Nullable String errorMessage,
		@Nullable String screenshotUrl,
		@Nullable String log,
		@Nullable Instant startedAt,
		@Nullable Instant finishedAt,
		@Nullable Long durationMs,
		Map<String, Object> stepSnapshot
) {
	public ExecutionStepResultView {
		stepSnapshot = stepSnapshot == null ? Map.of() : new LinkedHashMap<>(stepSnapshot);
	}
}
