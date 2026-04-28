package com.sondos.qa_buddy.workflows.repository;

import java.util.UUID;
import java.util.List;

import com.sondos.qa_buddy.workflows.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
	List<Workflow> findByDeletedAtIsNullOrderByUpdatedAtDesc();

	java.util.Optional<Workflow> findByIdAndDeletedAtIsNull(UUID id);
}
