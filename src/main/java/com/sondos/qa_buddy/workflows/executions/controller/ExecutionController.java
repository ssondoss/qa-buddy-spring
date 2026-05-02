package com.sondos.qa_buddy.workflows.executions.controller;

import java.util.UUID;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionView;
import com.sondos.qa_buddy.workflows.executions.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows/{workflowId}/versions")
@RequiredArgsConstructor
public class ExecutionController {
	private final ExecutionService executionService;

	@PostMapping("/{versionId}/execute")
	public ResponseEntity<ApiResponse<ExecutionView>> executeVersion(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID workflowId,
			@PathVariable UUID versionId
	) {
		ExecutionView response = executionService.executeVersion(jwt, workflowId, versionId);
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				"Version execution started",
				response
		));
	}
}
