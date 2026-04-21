package com.sondos.qa_buddy.users.dto;

public record ResendVerificationResponse(
		boolean verificationEmailSent,
		String message
) {
}
