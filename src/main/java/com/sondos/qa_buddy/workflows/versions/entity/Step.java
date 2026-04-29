package com.sondos.qa_buddy.workflows.versions.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.sondos.qa_buddy.shared.entity.AuditableEntity;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.versions.enums.StepAction;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
		name = "workflow_steps",
		uniqueConstraints = @UniqueConstraint(name = "uk_workflow_steps_version_order", columnNames = {
				"version_id", "order_index" }),
		indexes = @Index(name = "idx_workflow_steps_version", columnList = "version_id"))
public class Step extends AuditableEntity {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "version_id", nullable = false)
	private Version version;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private StepAction action;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StepSource source;

	@Column(length = 1000)
	private String selector;

	@Column(name = "input_value", length = 4000)
	private String value;

	@Column(name = "expected_value", length = 4000)
	private String expectedValue;

	@Builder.Default
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private Map<String, Object> meta = new LinkedHashMap<>();

	@Column(name = "is_optional", nullable = false)
	private boolean optional;

	@Column(name = "timeout_ms")
	private Integer timeoutMs;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;
}
