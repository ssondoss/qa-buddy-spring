package com.sondos.qa_buddy.workflows.versions.entity;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
		name = "workflow_versions",
		uniqueConstraints = @UniqueConstraint(name = "uk_workflow_versions_workflow_version", columnNames = {
				"workflow_id", "version_number" }),
		indexes = @Index(name = "idx_workflow_versions_workflow", columnList = "workflow_id"))
public class Version {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Workflow workflow;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private VersionStatus status;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Nullable
	@Column(name = "environment_name_snapshot", length = 160)
	private String environmentNameSnapshot;

	@Nullable
	@Column(name = "environment_web_base_url_snapshot", length = 1000)
	private String environmentWebBaseUrlSnapshot;

	@Nullable
	@Column(name = "environment_api_base_url_snapshot", length = 1000)
	private String environmentApiBaseUrlSnapshot;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public void updateStatus(VersionStatus status, Instant updatedAt) {
		this.status = status;
		this.updatedAt = updatedAt;
	}

	public void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void captureEnvironmentSnapshot(
			String environmentName,
			String environmentWebBaseUrl,
			@Nullable String environmentApiBaseUrl
	) {
		this.environmentNameSnapshot = Objects.requireNonNull(environmentName, "environmentName must not be null");
		this.environmentWebBaseUrlSnapshot = Objects.requireNonNull(
				environmentWebBaseUrl,
				"environmentWebBaseUrl must not be null"
		);
		this.environmentApiBaseUrlSnapshot = normalizeNullableBaseUrl(environmentApiBaseUrl);
	}

	public void beginRecording(
			String environmentName,
			String environmentWebBaseUrl,
			@Nullable String environmentApiBaseUrl,
			Instant updatedAt
	) {
		captureEnvironmentSnapshot(environmentName, environmentWebBaseUrl, environmentApiBaseUrl);
		this.status = VersionStatus.RECORDING;
		this.updatedAt = updatedAt;
	}

	private String normalizeNullableBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}

		String normalized = baseUrl.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
