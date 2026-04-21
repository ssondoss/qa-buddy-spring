package com.sondos.qa_buddy.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
		@Email @NotBlank String email
) {
}
