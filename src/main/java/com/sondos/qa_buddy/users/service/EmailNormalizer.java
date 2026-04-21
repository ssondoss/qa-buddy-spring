package com.sondos.qa_buddy.users.service;

import java.util.Locale;

public final class EmailNormalizer {
	private EmailNormalizer() {
	}

	public static String normalize(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
