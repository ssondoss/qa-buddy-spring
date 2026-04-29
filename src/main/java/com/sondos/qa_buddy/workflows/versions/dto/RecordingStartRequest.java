package com.sondos.qa_buddy.workflows.versions.dto;

import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.validation.RelativePath;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordingStartRequest(
		@NotNull UUID environmentId,
		@Size(max = 1000) @RelativePath String startPath
) {}
