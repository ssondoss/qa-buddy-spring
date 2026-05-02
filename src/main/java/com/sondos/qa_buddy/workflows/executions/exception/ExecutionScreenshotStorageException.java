package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionScreenshotStorageException extends RuntimeException {
	public ExecutionScreenshotStorageException(String message) {
		super(message);
	}

	public ExecutionScreenshotStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
