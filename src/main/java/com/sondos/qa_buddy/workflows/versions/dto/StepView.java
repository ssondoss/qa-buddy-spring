package com.sondos.qa_buddy.workflows.versions.dto;

import java.util.Map;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.enums.StepAction;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;

public record StepView(
		UUID id,
		int orderIndex,
		StepAction action,
		StepSource source,
		String selector,
		String value,
		String expectedValue,
		Map<String, Object> meta,
		boolean optional,
		Integer timeoutMs,
		int retryCount
) {}
