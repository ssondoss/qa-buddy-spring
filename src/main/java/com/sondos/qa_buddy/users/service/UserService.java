package com.sondos.qa_buddy.users.service;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.exception.EmailAlreadyRegisteredException;
import com.sondos.qa_buddy.users.exception.EmailNotVerifiedException;
import com.sondos.qa_buddy.users.exception.InvalidCredentialsException;
import com.sondos.qa_buddy.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final EmailVerificationService emailVerificationService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public RegistrationResult register(String email, String rawPassword) {
		String normalizedEmail = EmailNormalizer.normalize(email);

		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new EmailAlreadyRegisteredException();
		}

		User user = User.builder()
				.id(UUID.randomUUID())
				.email(normalizedEmail)
				.passwordHash(passwordEncoder.encode(rawPassword))
				.createdAt(Instant.now())
				.emailVerified(false)
				.emailVerifiedAt(null)
				.build();

		User savedUser = userRepository.save(user);
		boolean verificationEmailSent = emailVerificationService.createAndSendVerification(savedUser);
		return new RegistrationResult(savedUser, verificationEmailSent);
	}

	@Transactional(readOnly = true)
	public User authenticate(String email, String rawPassword) {
		String normalizedEmail = EmailNormalizer.normalize(email);

		User user = userRepository.findByEmail(normalizedEmail)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		if (!user.isEmailVerified()) {
			throw new EmailNotVerifiedException();
		}

		return user;
	}
}

