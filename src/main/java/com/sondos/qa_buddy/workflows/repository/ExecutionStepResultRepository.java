package com.sondos.qa_buddy.workflows.repository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.entity.ExecutionStepResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepResultRepository extends JpaRepository<ExecutionStepResult, UUID> {
	List<ExecutionStepResult> findByExecutionIdOrderByOrderIndexAsc(UUID executionId);
}
