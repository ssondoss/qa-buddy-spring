package com.sondos.qa_buddy.workflows.environments.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.environments.dto.EnvironmentRequest;
import com.sondos.qa_buddy.workflows.environments.dto.EnvironmentResponse;
import com.sondos.qa_buddy.workflows.exception.EnvironmentAlreadyExistsException;
import com.sondos.qa_buddy.workflows.exception.EnvironmentNotFoundException;
import com.sondos.qa_buddy.workflows.environments.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnvironmentService {
	private final CurrentUserService currentUserService;
	private final EnvironmentRepository environmentRepository;

	public EnvironmentResponse create(Jwt jwt, EnvironmentRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		String normalizedName = normalizeName(request.name());

		if (environmentRepository.existsByCreatedByIdAndNameAndDeletedAtIsNull(user.getId(), normalizedName)) {
			throw new EnvironmentAlreadyExistsException();
		}

		Instant now = Instant.now();
		Environment savedEnvironment = environmentRepository.save(
				Environment.builder()
						.id(UUID.randomUUID())
						.createdBy(user)
						.name(normalizedName)
						.webBaseUrl(normalizeBaseUrl(request.webBaseUrl()))
						.apiBaseUrl(normalizeBaseUrl(request.apiBaseUrl()))
						.createdAt(now)
						.updatedAt(now)
						.build()
		);

		return toResponse(savedEnvironment);
	}

	@Transactional(readOnly = true)
	public List<EnvironmentResponse> getAll(Jwt jwt) {
		User user = currentUserService.getCurrentUser(jwt);
		return environmentRepository.findByCreatedByIdAndDeletedAtIsNullOrderByNameAsc(user.getId())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public EnvironmentResponse getById(Jwt jwt, UUID environmentId) {
		return toResponse(getRequiredEnvironment(jwt, environmentId));
	}

	public EnvironmentResponse update(Jwt jwt, UUID environmentId, EnvironmentRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Environment environment = getRequiredEnvironment(user.getId(), environmentId);
		String normalizedName = normalizeName(request.name());

		if (!environment.getName().equals(normalizedName)
				&& environmentRepository.existsByCreatedByIdAndNameAndDeletedAtIsNull(user.getId(), normalizedName)) {
			throw new EnvironmentAlreadyExistsException();
		}

		environment.updateDetails(
				normalizedName,
				normalizeBaseUrl(request.webBaseUrl()),
				normalizeBaseUrl(request.apiBaseUrl()),
				Instant.now()
		);
		environmentRepository.save(environment);
		return toResponse(environment);
	}

	public void delete(Jwt jwt, UUID environmentId) {
		User user = currentUserService.getCurrentUser(jwt);
		Environment environment = getRequiredEnvironment(user.getId(), environmentId);
		environment.markDeleted(user, Instant.now());
		environmentRepository.save(environment);
	}

	@Transactional(readOnly = true)
	public Environment getRequiredEnvironment(Jwt jwt, UUID environmentId) {
		User user = currentUserService.getCurrentUser(jwt);
		return getRequiredEnvironment(user.getId(), environmentId);
	}

	@Transactional(readOnly = true)
	public Environment getRequiredEnvironment(UUID userId, UUID environmentId) {
		return environmentRepository.findByIdAndCreatedByIdAndDeletedAtIsNull(environmentId, userId)
				.orElseThrow(EnvironmentNotFoundException::new);
	}

	private EnvironmentResponse toResponse(Environment environment) {
		return new EnvironmentResponse(
				environment.getId(),
				environment.getName(),
				environment.getWebBaseUrl(),
				environment.getApiBaseUrl(),
				environment.getCreatedAt(),
				environment.getUpdatedAt()
		);
	}

	private String normalizeName(String name) {
		return name.trim();
	}

	private String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}

		String normalized = baseUrl.trim();
		if (normalized.isEmpty()) {
			return null;
		}

		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
