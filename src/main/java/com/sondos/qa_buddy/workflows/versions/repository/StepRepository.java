package com.sondos.qa_buddy.workflows.versions.repository;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRepository extends JpaRepository<Step, UUID> {
	List<Step> findByVersionIdOrderByOrderIndexAsc(UUID versionId);

	void deleteByVersionId(UUID versionId);
}
