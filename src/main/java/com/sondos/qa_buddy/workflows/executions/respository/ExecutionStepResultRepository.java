package com.sondos.qa_buddy.workflows.executions.respository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.entity.ExecutionStepResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepResultRepository extends JpaRepository<ExecutionStepResult, UUID> {
	List<ExecutionStepResult> findByExecutionIdOrderByOrderIndexAsc(UUID executionId);
}
