package com.sondos.qa_buddy.users.mapper;

import com.sondos.qa_buddy.security.JwtToken;
import com.sondos.qa_buddy.users.dto.AuthResponse;
import com.sondos.qa_buddy.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
	public AuthResponse toAuthResponse(User user, JwtToken token) {
		return new AuthResponse(
				token.value(),
				"Bearer",
				token.expiresAt(),
				user.getId(),
				user.getEmail()
		);
	}
}
