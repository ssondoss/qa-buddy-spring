package com.sondos.qa_buddy.workflows.versions.dto;

import java.util.Map;

import com.sondos.qa_buddy.workflows.versions.enums.StepAction;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StepRequest(
		@NotNull StepAction action,
		StepSource source,
		@Size(max = 1000) String selector,
		@Size(max = 4000) String value,
		@Size(max = 4000) String expectedValue,
		Map<String, Object> meta,
		Boolean optional,
		Integer timeoutMs,
		@Min(0) Integer retryCount
) {}
