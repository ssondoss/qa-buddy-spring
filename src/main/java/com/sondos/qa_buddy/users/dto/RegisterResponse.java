package com.sondos.qa_buddy.users.dto;

import java.util.UUID;

public record RegisterResponse(
		UUID userId,
		String email,
		boolean verificationRequired,
		boolean verificationEmailSent,
		String message
) {
}
