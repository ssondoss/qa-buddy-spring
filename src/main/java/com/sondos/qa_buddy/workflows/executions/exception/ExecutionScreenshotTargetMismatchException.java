package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionScreenshotTargetMismatchException extends RuntimeException {
	public ExecutionScreenshotTargetMismatchException() {
		super("Step does not belong to this execution version");
	}
}
