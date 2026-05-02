package com.sondos.qa_buddy.workflows.versions.dto;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import org.jspecify.annotations.Nullable;

public record VersionView(
		UUID workflowId,
		UUID versionId,
		int versionNumber,
		VersionStatus status,
		@Nullable String environmentNameSnapshot,
		@Nullable String environmentWebBaseUrlSnapshot,
		@Nullable String environmentApiBaseUrlSnapshot,
		Instant createdAt,
		Instant updatedAt
) {}
