package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionScreenshotNotFoundException extends RuntimeException {
	public ExecutionScreenshotNotFoundException() {
		super("Screenshot not found");
	}
}
