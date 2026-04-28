package com.sondos.qa_buddy.workflows.dto;

import java.util.List;

import com.sondos.qa_buddy.workflows.enums.WorkflowVersionStatus;
import jakarta.validation.Valid;

public record WorkflowVersionCompleteRequest(
		WorkflowVersionStatus status,
		@Valid List<@Valid WorkflowStepRequest> steps
) {}
