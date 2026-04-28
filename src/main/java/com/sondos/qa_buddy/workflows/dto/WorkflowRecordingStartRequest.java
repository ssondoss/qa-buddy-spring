package com.sondos.qa_buddy.workflows.dto;

import java.util.UUID;

import com.sondos.qa_buddy.workflows.recording.validation.RelativePath;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkflowRecordingStartRequest(
		@NotNull UUID environmentId,
		@Size(max = 1000) @RelativePath String startPath
) {}
