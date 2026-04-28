package com.sondos.qa_buddy.workflows.repository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
	List<WorkflowStep> findByWorkflowVersionIdOrderByOrderIndexAsc(UUID workflowVersionId);

	void deleteByWorkflowVersionId(UUID workflowVersionId);
}
