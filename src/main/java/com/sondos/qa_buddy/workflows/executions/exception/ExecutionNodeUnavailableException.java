package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionNodeUnavailableException extends RuntimeException {
	public ExecutionNodeUnavailableException(String message) {
		super(message);
	}

	public ExecutionNodeUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
