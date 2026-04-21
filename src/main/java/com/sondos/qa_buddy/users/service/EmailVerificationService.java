package com.sondos.qa_buddy.users.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.shared.email.EmailService;
import com.sondos.qa_buddy.users.entity.EmailVerificationToken;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.exception.InvalidVerificationTokenException;
import com.sondos.qa_buddy.users.repository.EmailVerificationTokenRepository;
import com.sondos.qa_buddy.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
	private final EmailVerificationTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Value("${app.mail.verification-base-url:http://localhost:8080}")
	private String verificationBaseUrl;

	@Value("${app.mail.verification-ttl:PT24H}")
	private Duration verificationTtl;

	@Value("${app.mail.resend-cooldown:PT1M}")
	private Duration resendCooldown;

	@Value("${app.mail.max-resend-attempts-per-day:5}")
	private int maxResendAttemptsPerDay;

	@Transactional
	public boolean createAndSendVerification(User user) {
		Instant now = Instant.now();
		EmailVerificationToken verificationToken = EmailVerificationToken.builder()
				.id(UUID.randomUUID())
				.user(user)
				.token(UUID.randomUUID().toString())
				.createdAt(now)
				.expiresAt(now.plus(verificationTtl))
				.usedAt(null)
				.build();

		tokenRepository.save(verificationToken);
		return emailService.sendVerificationEmail(user, buildVerificationUrl(verificationToken.getToken()));
	}

	@Transactional
	public void verifyToken(String rawToken) {
		Instant now = Instant.now();
		EmailVerificationToken verificationToken = tokenRepository.findByToken(rawToken)
				.orElseThrow(InvalidVerificationTokenException::new);

		if (!verificationToken.isUsableAt(now)) {
			throw new InvalidVerificationTokenException();
		}

		verificationToken.markUsed(now);
		User user = verificationToken.getUser();
		user.markEmailVerified(now);
		userRepository.save(user);
	}

	@Transactional
	public boolean resendVerification(String email) {
		String normalizedEmail = EmailNormalizer.normalize(email);
		return userRepository.findByEmail(normalizedEmail)
				.filter(user -> !user.isEmailVerified())
				.filter(this::canSendVerification)
				.map(this::createAndSendVerification)
				.orElse(false);
	}

	private boolean canSendVerification(User user) {
		Instant now = Instant.now();
		Instant attemptsWindowStart = now.minus(Duration.ofDays(1));

		if (tokenRepository.countByUserAndCreatedAtAfter(user, attemptsWindowStart) >= maxResendAttemptsPerDay) {
			return false;
		}

		return tokenRepository.findTopByUserOrderByCreatedAtDesc(user)
				.map(latestToken -> latestToken.getCreatedAt().plus(resendCooldown).isBefore(now))
				.orElse(true);
	}

	private String buildVerificationUrl(String token) {
		return verificationBaseUrl + "/api/auth/verify-email?token=" + token;
	}
}
