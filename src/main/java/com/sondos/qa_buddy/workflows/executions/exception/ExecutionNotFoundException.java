package com.sondos.qa_buddy.workflows.executions.exception;

public class ExecutionNotFoundException extends RuntimeException {
	public ExecutionNotFoundException() {
		super("Execution not found");
	}
}
