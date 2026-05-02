package com.sondos.qa_buddy.workflows.executions.entity;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStatus;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
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
		name = "workflow_executions",
		indexes = {
				@Index(name = "idx_workflow_executions_workflow", columnList = "workflow_id"),
				@Index(name = "idx_workflow_executions_version", columnList = "workflow_version_id")
		})
public class Execution {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Workflow workflow;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_version_id", nullable = false)
	private Version version;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ExecutionStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "triggered_by", nullable = false)
	private User triggeredBy;

	@Nullable
	@Column(name = "environment_name_snapshot", length = 160)
	private String environmentNameSnapshot;

	@Nullable
	@Column(name = "environment_web_base_url_snapshot", length = 1000)
	private String environmentWebBaseUrlSnapshot;

	@Nullable
	@Column(name = "environment_api_base_url_snapshot", length = 1000)
	private String environmentApiBaseUrlSnapshot;

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

	public void updateOutcome(ExecutionStatus status, Instant startedAt, @Nullable Instant finishedAt) {
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
		this.finishedAt = finishedAt;
	}

	public void markFailed(Instant finishedAt) {
		this.status = ExecutionStatus.FAILED;
		this.finishedAt = Objects.requireNonNull(finishedAt, "finishedAt must not be null");
	}

	private String normalizeNullableBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}

		String normalized = baseUrl.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
