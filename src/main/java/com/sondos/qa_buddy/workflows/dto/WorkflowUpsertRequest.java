package com.sondos.qa_buddy.workflows.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowUpsertRequest(
		@NotBlank @Size(max = 160) String name,
		@Size(max = 2000) String description
) {}
