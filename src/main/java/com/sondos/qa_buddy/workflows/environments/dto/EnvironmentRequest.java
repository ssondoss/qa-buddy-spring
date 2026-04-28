package com.sondos.qa_buddy.workflows.environments.dto;

import com.sondos.qa_buddy.workflows.environments.validation.AbsoluteHttpUrl;
import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvironmentRequest(
		@NotBlank @Size(max = 160) String name,
		@NotBlank @Size(max = 1000) @AbsoluteHttpUrl String webBaseUrl,
		@Nullable @Size(max = 1000) @AbsoluteHttpUrl String apiBaseUrl
) {}
