package com.sondos.qa_buddy.users.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}

