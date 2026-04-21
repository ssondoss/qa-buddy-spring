package com.sondos.qa_buddy.shared.api;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
		boolean success,
		int status,
		String message,
		T data,
		Map<String, String> errors,
		Instant timestamp
) {
	public static <T> ApiResponse<T> success(int status, String message, T data) {
		return new ApiResponse<>(true, status, message, data, null, Instant.now());
	}

	public static <T> ApiResponse<T> failure(int status, String message, Map<String, String> errors) {
		return new ApiResponse<>(false, status, message, null, errors, Instant.now());
	}
}
