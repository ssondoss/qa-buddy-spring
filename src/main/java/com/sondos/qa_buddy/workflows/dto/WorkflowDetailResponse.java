package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.enums.WorkflowStatus;

public record WorkflowDetailResponse(
		UUID workflowId,
		UUID workflowVersionId,
		int versionNumber,
		String name,
		String description,
		WorkflowStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<WorkflowStepView> steps
) {}
