package com.sondos.qa_buddy.workflows.environments.dto;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record EnvironmentResponse(
		UUID id,
		String name,
		String webBaseUrl,
		@Nullable String apiBaseUrl,
		Instant createdAt,
		Instant updatedAt
) {}
