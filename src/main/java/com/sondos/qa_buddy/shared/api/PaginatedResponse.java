package com.sondos.qa_buddy.shared.api;

import java.util.List;

public record PaginatedResponse<T>(
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean last
) {}
