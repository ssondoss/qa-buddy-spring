package com.sondos.qa_buddy.workflows.versions.controller;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.versions.dto.StepReorderRequest;
import com.sondos.qa_buddy.workflows.versions.dto.StepRequest;
import com.sondos.qa_buddy.workflows.versions.dto.StepView;
import com.sondos.qa_buddy.workflows.versions.service.VersionStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/steps/{versionId}")
@RequiredArgsConstructor
public class StepController {
	private final VersionStepService versionStepService;

	@GetMapping
	public ApiResponse<List<StepView>> getAll(@PathVariable UUID versionId) {
		List<StepView> response = versionStepService.getAll(versionId);
		return ApiResponse.success(HttpStatus.OK.value(), "Steps fetched", response);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<StepView>> create(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID versionId,
			@Valid @RequestBody StepRequest request
	) {
		StepView response = versionStepService.create(jwt, versionId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "Step created", response));
	}

	@PutMapping("/{stepId}")
	public ApiResponse<StepView> update(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID versionId,
			@PathVariable UUID stepId,
			@Valid @RequestBody StepRequest request
	) {
		StepView response = versionStepService.update(jwt, versionId, stepId, request);
		return ApiResponse.success(HttpStatus.OK.value(), "Step updated", response);
	}

	@DeleteMapping("/{stepId}")
	public ApiResponse<Void> delete(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID versionId,
			@PathVariable UUID stepId
	) {
		versionStepService.delete(jwt, versionId, stepId);
		return ApiResponse.success(HttpStatus.OK.value(), "Step deleted", null);
	}

	@PatchMapping("/reorder")
	public ApiResponse<List<StepView>> reorder(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID versionId,
			@Valid @RequestBody StepReorderRequest request
	) {
		List<StepView> response = versionStepService.reorder(jwt, versionId, request);
		return ApiResponse.success(HttpStatus.OK.value(), "Steps reordered", response);
	}
}
