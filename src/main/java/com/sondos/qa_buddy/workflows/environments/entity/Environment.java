package com.sondos.qa_buddy.workflows.environments.entity;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.shared.entity.SoftDeletableEntity;
import com.sondos.qa_buddy.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
		name = "environments",
		indexes = {
				@Index(name = "idx_environments_owner", columnList = "created_by"),
				@Index(name = "idx_environments_owner_name_deleted", columnList = "created_by,name,deleted_at")
		})
public class Environment extends SoftDeletableEntity {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(name = "web_base_url", nullable = false, length = 1000)
	private String webBaseUrl;

	@Nullable
	@Column(name = "api_base_url", length = 1000)
	private String apiBaseUrl;

	public void updateDetails(String name, String webBaseUrl, @Nullable String apiBaseUrl, Instant updatedAt) {
		this.name = name;
		this.webBaseUrl = webBaseUrl;
		this.apiBaseUrl = apiBaseUrl;
		touch(updatedAt);
	}
}
