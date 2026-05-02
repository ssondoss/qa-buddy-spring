package com.sondos.qa_buddy.workflows.versions.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StepReorderRequest(
		@NotNull List<@NotNull UUID> stepIds
) {}
