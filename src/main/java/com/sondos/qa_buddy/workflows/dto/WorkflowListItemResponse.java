package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;

public record WorkflowListItemResponse(
		UUID workflowId,
		String name,
		String description,
		VersionStatus currentVersionStatus,
		int currentVersion,
		Instant createdAt,
		Instant updatedAt
) {}
