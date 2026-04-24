package com.sondos.qa_buddy.workflows.dto;

import java.util.Map;

import com.sondos.qa_buddy.workflows.enums.WorkflowStepAction;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkflowStepRequest(
		@NotNull WorkflowStepAction action,
		WorkflowStepSource source,
		@Size(max = 1000) String selector,
		@Size(max = 4000) String value,
		@Size(max = 4000) String expectedValue,
		Map<String, Object> meta,
		Boolean optional,
		Integer timeoutMs,
		@Min(0) Integer retryCount
) {}
