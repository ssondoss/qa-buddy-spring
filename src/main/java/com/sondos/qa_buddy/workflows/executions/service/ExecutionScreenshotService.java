package com.sondos.qa_buddy.workflows.executions.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import com.sondos.qa_buddy.workflows.executions.dto.ExecutionScreenshotUploadResponse;
import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionNotFoundException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotNotFoundException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotStorageException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotTargetMismatchException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotValidationException;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.exception.StepNotFoundException;
import com.sondos.qa_buddy.workflows.executions.respository.ExecutionRepository;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionScreenshotService {
	private final Path rootDir;
	private final ExecutionRepository executionRepository;
	private final StepRepository stepRepository;

	public ExecutionScreenshotService(
			@Value("${app.execution.screenshot-dir:uploads/screenshots}") String screenshotDir,
			ExecutionRepository executionRepository,
			StepRepository stepRepository
	) {
		this.rootDir = Paths.get(screenshotDir).toAbsolutePath().normalize();
		this.executionRepository = executionRepository;
		this.stepRepository = stepRepository;
	}

	@Transactional(readOnly = true)
	public ExecutionScreenshotUploadResponse save(UUID executionId, UUID stepId, MultipartFile file) {
		validateUploadRequest(file);
		validateExecutionAndStep(executionId, stepId);

		String fileName = UUID.randomUUID() + resolveExtension(file);
		Path target = resolveTarget(executionId, stepId, fileName);

		try {
			Files.createDirectories(target.getParent());
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			throw new ExecutionScreenshotStorageException("Unable to store screenshot", ex);
		}

		return new ExecutionScreenshotUploadResponse(
				executionId,
				stepId,
				fileName,
				buildScreenshotUrl(executionId, stepId, fileName)
		);
	}

	@Transactional(readOnly = true)
	public Resource load(UUID executionId, UUID stepId, String fileName) {
		validateExecutionAndStep(executionId, stepId);

		Path target = resolveTarget(executionId, stepId, fileName);
		if (!Files.exists(target) || !Files.isRegularFile(target)) {
			throw new ExecutionScreenshotNotFoundException();
		}

		return new FileSystemResource(target);
	}

	private void validateUploadRequest(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ExecutionScreenshotValidationException("Screenshot file is required");
		}
	}

	private void validateExecutionAndStep(UUID executionId, UUID stepId) {
		Execution execution = executionRepository.findById(executionId)
				.orElseThrow(ExecutionNotFoundException::new);
		Step step = stepRepository.findById(stepId)
				.orElseThrow(StepNotFoundException::new);

		if (!execution.getVersion().getId().equals(step.getVersion().getId())) {
			throw new ExecutionScreenshotTargetMismatchException();
		}
	}

	private Path resolveTarget(UUID executionId, UUID stepId, String fileName) {
		Path target = rootDir
				.resolve(executionId.toString())
				.resolve(stepId.toString())
				.resolve(fileName)
				.normalize();

		if (!target.startsWith(rootDir)) {
			throw new ExecutionScreenshotStorageException("Invalid screenshot path");
		}

		return target;
	}

	private String buildScreenshotUrl(UUID executionId, UUID stepId, String fileName) {
		return "/api/executions/%s/screenshots/%s/%s".formatted(executionId, stepId, fileName);
	}

	private String resolveExtension(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType != null) {
			String normalized = contentType.toLowerCase(Locale.ROOT);
			if ("image/png".equals(normalized)) {
				return ".png";
			}
			if ("image/jpeg".equals(normalized)) {
				return ".jpg";
			}
			if ("image/webp".equals(normalized)) {
				return ".webp";
			}
			if ("image/gif".equals(normalized)) {
				return ".gif";
			}
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename != null) {
			String lower = originalFilename.toLowerCase(Locale.ROOT);
			if (lower.endsWith(".png")) {
				return ".png";
			}
			if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
				return ".jpg";
			}
			if (lower.endsWith(".webp")) {
				return ".webp";
			}
			if (lower.endsWith(".gif")) {
				return ".gif";
			}
		}

		return ".png";
	}
}
