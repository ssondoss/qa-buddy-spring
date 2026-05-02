package com.sondos.qa_buddy.workflows.executions.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.enums.StepAction;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import org.jspecify.annotations.Nullable;

public record ExecutionStepRequest(
		UUID stepId,
		int orderIndex,
		StepAction action,
		StepSource source,
		@Nullable String selector,
		@Nullable String value,
		@Nullable String expectedValue,
		Map<String, Object> meta,
		boolean optional,
		@Nullable Integer timeoutMs,
		int retryCount
) {
	public ExecutionStepRequest {
		stepId = Objects.requireNonNull(stepId, "stepId must not be null");
		action = Objects.requireNonNull(action, "action must not be null");
		source = Objects.requireNonNull(source, "source must not be null");
		meta = meta == null ? new LinkedHashMap<>() : new LinkedHashMap<>(meta);
	}
}
