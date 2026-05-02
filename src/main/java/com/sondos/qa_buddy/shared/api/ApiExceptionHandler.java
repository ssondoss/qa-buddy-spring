package com.sondos.qa_buddy.shared.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sondos.qa_buddy.users.exception.EmailAlreadyRegisteredException;
import com.sondos.qa_buddy.users.exception.EmailNotVerifiedException;
import com.sondos.qa_buddy.users.exception.InvalidCredentialsException;
import com.sondos.qa_buddy.users.exception.InvalidVerificationTokenException;
import com.sondos.qa_buddy.security.AuthenticatedUserNotFoundException;
import com.sondos.qa_buddy.workflows.versions.exception.StepNotFoundException;
import com.sondos.qa_buddy.workflows.versions.exception.StepOrderMismatchException;
import com.sondos.qa_buddy.workflows.versions.exception.RecordingCallbackUnauthorizedException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionNotFoundException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotCallbackUnauthorizedException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotNotFoundException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotStorageException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotTargetMismatchException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionScreenshotValidationException;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionNodeUnavailableException;
import com.sondos.qa_buddy.workflows.executions.exception.VersionNotReadyException;
import com.sondos.qa_buddy.workflows.versions.exception.RecordingNodeUnavailableException;
import com.sondos.qa_buddy.workflows.environments.exceptions.EnvironmentAlreadyExistsException;
import com.sondos.qa_buddy.workflows.environments.exceptions.EnvironmentNotFoundException;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.versions.exception.VersionNotFoundException;
import com.sondos.qa_buddy.workflows.versions.exception.VersionLockedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
		return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ApiResponse<Void>> handleEmailNotVerified(EmailNotVerifiedException ex) {
		return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
	}

	@ExceptionHandler(InvalidVerificationTokenException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(AuthenticatedUserNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticatedUserNotFound(
			AuthenticatedUserNotFoundException ex
	) {
		return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
	}

	@ExceptionHandler(WorkflowNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleWorkflowNotFound(WorkflowNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(VersionNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleWorkflowVersionNotFound(VersionNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(VersionNotReadyException.class)
	public ResponseEntity<ApiResponse<Void>> handleVersionNotReady(VersionNotReadyException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	@ExceptionHandler(StepNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleStepNotFound(StepNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(VersionLockedException.class)
	public ResponseEntity<ApiResponse<Void>> handleVersionLocked(VersionLockedException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	@ExceptionHandler(StepOrderMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleStepOrderMismatch(StepOrderMismatchException ex) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(EnvironmentNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleEnvironmentNotFound(EnvironmentNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(EnvironmentAlreadyExistsException.class)
	public ResponseEntity<ApiResponse<Void>> handleEnvironmentAlreadyExists(EnvironmentAlreadyExistsException ex) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	@ExceptionHandler(RecordingCallbackUnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleRecordingCallbackUnauthorized(
			RecordingCallbackUnauthorizedException ex
	) {
		return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionScreenshotCallbackUnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionScreenshotCallbackUnauthorized(
			ExecutionScreenshotCallbackUnauthorizedException ex
	) {
		return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
	}

	@ExceptionHandler(RecordingNodeUnavailableException.class)
	public ResponseEntity<ApiResponse<Void>> handleRecordingNodeUnavailable(RecordingNodeUnavailableException ex) {
		return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionNotFound(ExecutionNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionScreenshotNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionScreenshotNotFound(ExecutionScreenshotNotFoundException ex) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionScreenshotTargetMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionScreenshotTargetMismatch(
			ExecutionScreenshotTargetMismatchException ex
	) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionScreenshotValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionScreenshotValidation(
			ExecutionScreenshotValidationException ex
	) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionScreenshotStorageException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionScreenshotStorage(
			ExecutionScreenshotStorageException ex
	) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
	}

	@ExceptionHandler(ExecutionNodeUnavailableException.class)
	public ResponseEntity<ApiResponse<Void>> handleExecutionNodeUnavailable(ExecutionNodeUnavailableException ex) {
		return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), null);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
			MissingServletRequestParameterException ex
	) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		return buildError(HttpStatus.CONFLICT, "Data integrity violation", null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return buildError(HttpStatus.BAD_REQUEST, "Validation error", errors);
	}

	private ResponseEntity<ApiResponse<Void>> buildError(
			HttpStatus status,
			String message,
			Map<String, String> errors
	) {
		return ResponseEntity.status(status)
				.body(ApiResponse.failure(status.value(), message, errors));
	}
}
