package com.sondos.qa_buddy.workflows.versions.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RelativePathValidator implements ConstraintValidator<RelativePath, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		return value.trim().startsWith("/");
	}
}
