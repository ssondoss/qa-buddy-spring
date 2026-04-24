package com.sondos.qa_buddy.workflows.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, UUID> {
	List<WorkflowVersion> findByWorkflowIdOrderByVersionNumberDesc(UUID workflowId);

	Optional<WorkflowVersion> findByWorkflowIdAndVersionNumber(UUID workflowId, int versionNumber);
}
