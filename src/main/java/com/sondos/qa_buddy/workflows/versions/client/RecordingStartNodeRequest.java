package com.sondos.qa_buddy.workflows.versions.client;

import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record RecordingStartNodeRequest(
		UUID workflowId,
		UUID workflowVersionId,
		int versionNumber,
		String workflowName,
		@Nullable String workflowDescription,
		String webBaseUrl,
		@Nullable String apiBaseUrl,
		@Nullable String startPath
) {
	public RecordingStartNodeRequest {
		workflowId = Objects.requireNonNull(workflowId, "workflowId must not be null");
		workflowVersionId = Objects.requireNonNull(workflowVersionId, "workflowVersionId must not be null");
		workflowName = Objects.requireNonNull(workflowName, "workflowName must not be null");
		webBaseUrl = Objects.requireNonNull(webBaseUrl, "webBaseUrl must not be null");
	}
}
