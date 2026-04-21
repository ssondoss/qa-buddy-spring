package com.sondos.qa_buddy.users.service;

import com.sondos.qa_buddy.users.entity.User;

public record RegistrationResult(
		User user,
		boolean verificationEmailSent
) {
}
