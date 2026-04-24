package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.enums.WorkflowStatus;

public record WorkflowListItemResponse(
		UUID workflowId,
		String name,
		String description,
		WorkflowStatus status,
		int currentVersion,
		Instant createdAt,
		Instant updatedAt
) {}
