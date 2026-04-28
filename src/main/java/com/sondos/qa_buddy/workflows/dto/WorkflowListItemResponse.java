package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.enums.WorkflowVersionStatus;

public record WorkflowListItemResponse(
		UUID workflowId,
		String name,
		String description,
		WorkflowVersionStatus currentVersionStatus,
		int currentVersion,
		Instant createdAt,
		Instant updatedAt
) {}
