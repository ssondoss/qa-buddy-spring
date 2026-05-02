package com.sondos.qa_buddy.workflows.executions.dto;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStepStatus;
import org.jspecify.annotations.Nullable;

public record ExecutionStepRunResponse(
		UUID stepId,
		int orderIndex,
		@Nullable ExecutionStepStatus status,
		@Nullable String errorMessage,
		@Nullable String screenshotUrl,
		@Nullable String log,
		@Nullable Instant startedAt,
		@Nullable Instant finishedAt,
		@Nullable Long durationMs
) {}
