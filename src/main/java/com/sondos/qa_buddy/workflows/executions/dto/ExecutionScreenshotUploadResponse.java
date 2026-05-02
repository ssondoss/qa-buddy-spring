package com.sondos.qa_buddy.workflows.executions.dto;

import java.util.UUID;

public record ExecutionScreenshotUploadResponse(
		UUID executionId,
		UUID stepId,
		String fileName,
		String screenshotUrl
) {
}
