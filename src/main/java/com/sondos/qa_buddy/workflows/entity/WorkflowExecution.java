package com.sondos.qa_buddy.workflows.entity;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.enums.WorkflowEnvironment;
import com.sondos.qa_buddy.workflows.enums.WorkflowExecutionStatus;
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
public class WorkflowExecution {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Workflow workflow;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_version_id", nullable = false)
	private WorkflowVersion workflowVersion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private WorkflowExecutionStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "triggered_by", nullable = false)
	private User triggeredBy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private WorkflowEnvironment environment;
}
