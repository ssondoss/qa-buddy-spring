package com.sondos.qa_buddy.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@Email @NotBlank String email,
		@NotBlank @Size(min = 1, max = 72) String password
) {}

