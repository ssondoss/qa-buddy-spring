package com.sondos.qa_buddy.users.application;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import com.sondos.qa_buddy.users.domain.User;
import com.sondos.qa_buddy.users.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User register(String email, String rawPassword) {
		String normalizedEmail = normalizeEmail(email);

		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new EmailAlreadyRegisteredException();
		}

		User user = new User(
				UUID.randomUUID(),
				normalizedEmail,
				passwordEncoder.encode(rawPassword),
				Instant.now()
		);

		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User authenticate(String email, String rawPassword) {
		String normalizedEmail = normalizeEmail(email);

		User user = userRepository.findByEmail(normalizedEmail)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return user;
	}

	private static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}

