package com.sondos.qa_buddy.users.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.EmailVerificationToken;
import com.sondos.qa_buddy.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
	Optional<EmailVerificationToken> findByToken(String token);

	Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);

	long countByUserAndCreatedAtAfter(User user, Instant createdAt);
}
