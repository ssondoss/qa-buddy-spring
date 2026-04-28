package com.sondos.qa_buddy.workflows.environments.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {
	List<Environment> findByCreatedByIdAndDeletedAtIsNullOrderByNameAsc(UUID createdById);

	Optional<Environment> findByIdAndCreatedByIdAndDeletedAtIsNull(UUID id, UUID createdById);

	boolean existsByCreatedByIdAndNameAndDeletedAtIsNull(UUID createdById, String name);
}
