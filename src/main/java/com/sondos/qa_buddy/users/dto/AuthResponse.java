package com.sondos.qa_buddy.users.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
		String accessToken,
		String tokenType,
		Instant expiresAt,
		UUID userId,
		String email
) {}

