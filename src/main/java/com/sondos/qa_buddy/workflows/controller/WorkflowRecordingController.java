package com.sondos.qa_buddy.workflows.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowRecordingStartRequest;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowVersionCompleteRequest;
import com.sondos.qa_buddy.workflows.exception.RecordingCallbackUnauthorizedException;
import com.sondos.qa_buddy.workflows.service.WorkflowVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkflowRecordingController {
	private final WorkflowVersionService workflowVersionService;
	private final String sharedSecret;

	public WorkflowRecordingController(
			WorkflowVersionService workflowVersionService,
			@Value("${app.recording.shared-secret:qa-buddy-recording-shared-secret}") String sharedSecret
	) {
		this.workflowVersionService = workflowVersionService;
		this.sharedSecret = sharedSecret;
	}

	@PostMapping("/workflows/{workflowId}/versions/start")
	public ResponseEntity<ApiResponse<WorkflowResponse>> startRecording(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID workflowId,
			@Valid @RequestBody WorkflowRecordingStartRequest request
	) {
		WorkflowResponse response = workflowVersionService.startRecording(jwt, workflowId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Recording started", response));
	}

	@PostMapping("/workflow-versions/{workflowVersionId}/complete")
	public ResponseEntity<ApiResponse<WorkflowResponse>> completeRecording(
			@PathVariable UUID workflowVersionId,
			@RequestHeader(value = "X-Integration-Secret", required = false) String recordingSecret,
			@Valid @RequestBody WorkflowVersionCompleteRequest request
	) {
		if (!this.sharedSecret.equals(recordingSecret)) {
			throw new RecordingCallbackUnauthorizedException();
		}

		WorkflowResponse response = workflowVersionService.completeRecording(workflowVersionId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Recording completed", response));
	}
}
