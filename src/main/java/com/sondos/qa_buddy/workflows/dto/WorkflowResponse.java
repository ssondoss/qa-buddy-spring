package com.sondos.qa_buddy.workflows.dto;

import java.util.UUID;

import com.sondos.qa_buddy.workflows.enums.WorkflowStatus;

public record WorkflowResponse(
		UUID workflowId,
		UUID workflowVersionId,
		int versionNumber,
		int stepCount,
		WorkflowStatus status
) {}
