package com.sondos.qa_buddy.workflows.dto;

import java.util.List;

import com.sondos.qa_buddy.workflows.enums.WorkflowStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowRequest(
		@NotBlank @Size(max = 160) String name,
		@Size(max = 2000) String description,
		WorkflowStatus status,
		@Valid List<@Valid WorkflowStepRequest> steps
) {}
