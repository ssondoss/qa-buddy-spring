package com.sondos.qa_buddy.workflows.versions.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.dto.WorkflowResponse;
import com.sondos.qa_buddy.workflows.versions.dto.RecordingStartRequest;
import com.sondos.qa_buddy.workflows.versions.dto.RecordingCompleteRequest;
import com.sondos.qa_buddy.workflows.versions.exception.RecordingCallbackUnauthorizedException;
import com.sondos.qa_buddy.workflows.versions.service.VersionService;
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
@RequestMapping("/api/version/recording")
public class RecordingController {
	private final VersionService versionService;
	private final String sharedSecret;

	public RecordingController(
			VersionService versionService,
			@Value("${app.recording.shared-secret:qa-buddy-recording-shared-secret}") String sharedSecret
	) {
		this.versionService = versionService;
		this.sharedSecret = sharedSecret;
	}

	@PostMapping("/start/{workflowId}")
	public ResponseEntity<ApiResponse<WorkflowResponse>> startRecording(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID workflowId,
			@Valid @RequestBody RecordingStartRequest request
	) {
		WorkflowResponse response = versionService.startRecording(jwt, workflowId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Recording started", response));
	}

	@PostMapping("/complete/{workflowVersionId}")
	public ResponseEntity<ApiResponse<WorkflowResponse>> completeRecording(
			@PathVariable("workflowVersionId") UUID workflowVersionId,
			@RequestHeader(value = "X-Integration-Secret", required = false) String recordingSecret,
			@Valid @RequestBody RecordingCompleteRequest request
	) {
		if (!this.sharedSecret.equals(recordingSecret)) {
			throw new RecordingCallbackUnauthorizedException();
		}

		WorkflowResponse response = versionService.completeRecording(workflowVersionId, request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Recording completed", response));
	}
}
