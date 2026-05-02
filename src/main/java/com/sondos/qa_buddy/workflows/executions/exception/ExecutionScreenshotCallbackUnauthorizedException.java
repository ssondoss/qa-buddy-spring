package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionScreenshotCallbackUnauthorizedException extends RuntimeException {
	public ExecutionScreenshotCallbackUnauthorizedException() {
		super("Invalid execution screenshot callback secret");
	}
}
