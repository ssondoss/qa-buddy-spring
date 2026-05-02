package com.sondos.qa_buddy.workflows.executions.controller;

import java.util.UUID;
import java.util.Locale;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionScreenshotUploadResponse;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotCallbackUnauthorizedException;
import com.sondos.qa_buddy.workflows.executions.service.ExecutionScreenshotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/executions/{executionId}/screenshots")
public class ExecutionScreenshotController {
	private final ExecutionScreenshotService executionScreenshotService;
	private final String sharedSecret;

	public ExecutionScreenshotController(
			ExecutionScreenshotService executionScreenshotService,
			@Value("${app.execution.shared-secret:qa-buddy-execution-shared-secret}") String sharedSecret
	) {
		this.executionScreenshotService = executionScreenshotService;
		this.sharedSecret = sharedSecret;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ExecutionScreenshotUploadResponse>> uploadScreenshot(
			@PathVariable UUID executionId,
			@RequestHeader(value = "X-Integration-Secret", required = false) String integrationSecret,
			@RequestParam UUID stepId,
			@RequestPart("file") MultipartFile file
	) {
		if (!sharedSecret.equals(integrationSecret)) {
			throw new ExecutionScreenshotCallbackUnauthorizedException();
		}

		ExecutionScreenshotUploadResponse response = executionScreenshotService.save(executionId, stepId, file);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "Screenshot uploaded successfully", response));
	}

	@GetMapping("/{stepId}/{fileName:.+}")
	public ResponseEntity<Resource> downloadScreenshot(
			@PathVariable UUID executionId,
			@PathVariable UUID stepId,
			@PathVariable String fileName
	) {
		Resource resource = executionScreenshotService.load(executionId, stepId, fileName);
		return ResponseEntity.ok()
				.contentType(resolveMediaType(fileName))
				.body(resource);
	}

	private MediaType resolveMediaType(String fileName) {
		String lower = fileName.toLowerCase(Locale.ROOT);
		if (lower.endsWith(".png")) {
			return MediaType.IMAGE_PNG;
		}
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			return MediaType.IMAGE_JPEG;
		}
		if (lower.endsWith(".gif")) {
			return MediaType.IMAGE_GIF;
		}
		if (lower.endsWith(".webp")) {
			return MediaType.parseMediaType("image/webp");
		}

		return MediaType.APPLICATION_OCTET_STREAM;
	}
}
