package com.sondos.qa_buddy.workflows.entity;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.enums.ExecutionStepStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
		name = "execution_step_results",
		indexes = {
				@Index(name = "idx_execution_step_results_execution", columnList = "execution_id"),
				@Index(name = "idx_execution_step_results_step", columnList = "step_id")
		})
public class ExecutionStepResult {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "execution_id", nullable = false)
	private WorkflowExecution execution;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "step_id", nullable = false)
	private WorkflowStep step;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ExecutionStepStatus status;

	@Column(name = "error_message", length = 4000)
	private String errorMessage;

	@Column(name = "screenshot_url", length = 1000)
	private String screenshotUrl;

	@Column(length = 10000)
	private String log;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@Column(name = "duration_ms")
	private Long durationMs;

	@Builder.Default
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "step_snapshot", nullable = false)
	private Map<String, Object> stepSnapshot = new LinkedHashMap<>();
}
