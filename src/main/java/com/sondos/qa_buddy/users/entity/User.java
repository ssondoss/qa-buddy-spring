package com.sondos.qa_buddy.users.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
	@Id
	private UUID id;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 72)
	private String passwordHash;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	public void markEmailVerified(Instant verifiedAt) {
		this.emailVerified = true;
		this.emailVerifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");
	}
}

