package com.sondos.qa_buddy.workflows.versions.dto;

import java.util.List;

import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import jakarta.validation.Valid;

public record RecordingCompleteRequest(
		VersionStatus status,
		@Valid List<@Valid StepRequest> steps
) {}
