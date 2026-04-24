package com.sondos.qa_buddy.shared.entity;

import java.time.Instant;
import java.util.Objects;

import com.sondos.qa_buddy.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SoftDeletableEntity extends AuditableEntity {
	@Column(name = "deleted_at")
	private Instant deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deleted_by")
	private User deletedBy;

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void markDeleted(User deletedBy, Instant deletedAt) {
		this.deletedBy = Objects.requireNonNull(deletedBy, "deletedBy must not be null");
		this.deletedAt = Objects.requireNonNull(deletedAt, "deletedAt must not be null");
	}
}
