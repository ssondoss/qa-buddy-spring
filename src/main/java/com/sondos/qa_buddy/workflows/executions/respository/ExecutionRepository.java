package com.sondos.qa_buddy.workflows.executions.respository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
	List<Execution> findByWorkflowIdOrderByStartedAtDesc(UUID workflowId);
}
