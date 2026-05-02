package com.sondos.qa_buddy.workflows.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowResponse(
		UUID id,
		String name,
		String description,
		int versionsCount,
		Instant createdAt,
		Instant updatedAt
) {}
