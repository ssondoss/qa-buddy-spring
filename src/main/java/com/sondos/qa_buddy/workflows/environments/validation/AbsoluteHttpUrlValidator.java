package com.sondos.qa_buddy.workflows.environments.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AbsoluteHttpUrlValidator implements ConstraintValidator<AbsoluteHttpUrl, String> {
	private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		try {
			URI uri = new URI(value.trim());
			if (!uri.isAbsolute()) {
				return false;
			}

			String scheme = uri.getScheme();
			if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
				return false;
			}

			return uri.getHost() != null && !uri.getHost().isBlank();
		} catch (URISyntaxException ex) {
			return false;
		}
	}
}
