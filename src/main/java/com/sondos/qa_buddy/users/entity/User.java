package com.sondos.qa_buddy.users.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

	protected User() {
	}

	public User(UUID id, String email, String passwordHash, Instant createdAt) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.email = Objects.requireNonNull(email, "email must not be null");
		this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}

