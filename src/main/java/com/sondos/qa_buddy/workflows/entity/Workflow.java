package com.sondos.qa_buddy.workflows.entity;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.shared.entity.SoftDeletableEntity;
import com.sondos.qa_buddy.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "workflows")
public class Workflow extends SoftDeletableEntity {
	@Id
	private UUID id;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(length = 2000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	public void updateDetails(String name, String description, Instant updatedAt) {
		this.name = name;
		this.description = description;
		touch(updatedAt);
	}
}
