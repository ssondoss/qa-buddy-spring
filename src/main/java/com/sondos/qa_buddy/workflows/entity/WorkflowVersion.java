package com.sondos.qa_buddy.workflows.entity;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class WorkflowVersion {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Workflow workflow;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
