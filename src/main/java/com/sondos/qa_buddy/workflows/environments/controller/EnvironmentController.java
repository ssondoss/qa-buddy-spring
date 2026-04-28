package com.sondos.qa_buddy.workflows.environments.controller;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.environments.dto.EnvironmentRequest;
import com.sondos.qa_buddy.workflows.environments.dto.EnvironmentResponse;
import com.sondos.qa_buddy.workflows.environments.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvironmentController {
	private final EnvironmentService environmentService;

	@PostMapping
	public ResponseEntity<ApiResponse<EnvironmentResponse>> create(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody EnvironmentRequest request
	) {
		EnvironmentResponse response = environmentService.create(jwt, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "Environment created", response));
	}

	@GetMapping
	public ApiResponse<List<EnvironmentResponse>> getAll(@AuthenticationPrincipal Jwt jwt) {
		List<EnvironmentResponse> response = environmentService.getAll(jwt);
		return ApiResponse.success(HttpStatus.OK.value(), "Environments fetched", response);
	}

	@GetMapping("/{environmentId}")
	public ApiResponse<EnvironmentResponse> getById(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID environmentId
	) {
		EnvironmentResponse response = environmentService.getById(jwt, environmentId);
		return ApiResponse.success(HttpStatus.OK.value(), "Environment fetched", response);
	}

	@PutMapping("/{environmentId}")
	public ApiResponse<EnvironmentResponse> update(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID environmentId,
			@Valid @RequestBody EnvironmentRequest request
	) {
		EnvironmentResponse response = environmentService.update(jwt, environmentId, request);
		return ApiResponse.success(HttpStatus.OK.value(), "Environment updated", response);
	}

	@DeleteMapping("/{environmentId}")
	public ApiResponse<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID environmentId) {
		environmentService.delete(jwt, environmentId);
		return ApiResponse.success(HttpStatus.OK.value(), "Environment deleted", null);
	}
}
