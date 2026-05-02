package com.sondos.qa_buddy.workflows.versions.controller;

import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.workflows.versions.dto.VersionView;
import com.sondos.qa_buddy.workflows.versions.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows/{workflowId}/versions")
@RequiredArgsConstructor
public class VersionController {
	private final VersionService versionService;

	@GetMapping
	public ApiResponse<List<VersionView>> getByWorkflowId(@PathVariable UUID workflowId) {
		List<VersionView> response = versionService.getByWorkflowId(workflowId);
		return ApiResponse.success(HttpStatus.OK.value(), "Versions fetched", response);
	}
}
