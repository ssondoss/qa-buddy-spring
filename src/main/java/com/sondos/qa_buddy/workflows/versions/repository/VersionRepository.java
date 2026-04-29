package com.sondos.qa_buddy.workflows.versions.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.versions.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionRepository extends JpaRepository<Version, UUID> {
	List<Version> findByWorkflowIdOrderByVersionNumberDesc(UUID workflowId);

	Optional<Version> findByWorkflowIdAndVersionNumber(UUID workflowId, int versionNumber);
}
