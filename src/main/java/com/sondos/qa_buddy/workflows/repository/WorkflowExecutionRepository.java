package com.sondos.qa_buddy.workflows.repository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.entity.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
	List<WorkflowExecution> findByWorkflowIdOrderByStartedAtDesc(UUID workflowId);
}
