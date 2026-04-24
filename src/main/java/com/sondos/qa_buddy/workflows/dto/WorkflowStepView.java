package com.sondos.qa_buddy.workflows.dto;

import java.util.Map;

import com.sondos.qa_buddy.workflows.enums.WorkflowStepAction;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;

public record WorkflowStepView(
		int orderIndex,
		WorkflowStepAction action,
		WorkflowStepSource source,
		String selector,
		String value,
		String expectedValue,
		Map<String, Object> meta,
		boolean optional,
		Integer timeoutMs,
		int retryCount
) {}
