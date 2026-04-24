package com.sondos.qa_buddy.workflows.repository;

import java.util.UUID;

import com.sondos.qa_buddy.workflows.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
	Page<Workflow> findByDeletedAtIsNull(Pageable pageable);

	java.util.Optional<Workflow> findByIdAndDeletedAtIsNull(UUID id);
}
