package com.sondos.qa_buddy.workflows.controller;

import java.util.UUID;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.shared.api.PaginatedResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowDetailResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowListItemResponse;
import com.sondos.qa_buddy.workflows.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
	private final WorkflowService workflowService;

	@PostMapping
	public ResponseEntity<ApiResponse<WorkflowResponse>> create(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody WorkflowRequest request
	) {
		WorkflowResponse response = workflowService.create(jwt, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "Workflow created", response));
	}

	@PutMapping("/{workflowId}")
	public ApiResponse<WorkflowResponse> update(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID workflowId,
			@Valid @RequestBody WorkflowRequest request
	) {
		WorkflowResponse response = workflowService.update(jwt, workflowId, request);
		return ApiResponse.success(HttpStatus.OK.value(), "Workflow updated", response);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PaginatedResponse<WorkflowListItemResponse>>> getAll(
			@PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		PaginatedResponse<WorkflowListItemResponse> response = workflowService.getAll(pageable);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Workflows fetched", response));
	}

	@GetMapping("/{workflowId}")
	public ApiResponse<WorkflowDetailResponse> getById(@PathVariable UUID workflowId) {
		WorkflowDetailResponse response = workflowService.getById(workflowId);
		return ApiResponse.success(HttpStatus.OK.value(), "Workflow fetched", response);
	}
}
