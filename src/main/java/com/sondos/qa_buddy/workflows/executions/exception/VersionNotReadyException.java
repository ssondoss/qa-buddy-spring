package com.sondos.qa_buddy.workflows.executions.exception;

public class VersionNotReadyException extends RuntimeException {
	public VersionNotReadyException() {
		super("Workflow version is not ready for execution");
	}
}
