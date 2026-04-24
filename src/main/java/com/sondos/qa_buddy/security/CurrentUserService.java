package com.sondos.qa_buddy.security;

import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getCurrentUser(Jwt jwt) {
		UUID userId = getCurrentUserId(jwt);
		return userRepository.findById(userId)
				.orElseThrow(AuthenticatedUserNotFoundException::new);
	}

	public UUID getCurrentUserId(Jwt jwt) {
		String userIdClaim = jwt.getClaimAsString("uid");
		if (userIdClaim == null || userIdClaim.isBlank()) {
			throw new AuthenticatedUserNotFoundException();
		}

		try {
			return UUID.fromString(userIdClaim);
		} catch (IllegalArgumentException ex) {
			throw new AuthenticatedUserNotFoundException();
		}
	}
}
