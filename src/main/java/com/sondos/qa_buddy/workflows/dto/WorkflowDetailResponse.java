package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.dto.StepView;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;

public record WorkflowDetailResponse(
		UUID workflowId,
		UUID workflowVersionId,
		int versionNumber,
		String name,
		String description,
		VersionStatus versionStatus,
		Instant createdAt,
		Instant updatedAt,
		List<StepView> steps
) {}
