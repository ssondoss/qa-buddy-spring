package com.sondos.qa_buddy.workflows.exception;

public class WorkflowVersionNotFoundException extends RuntimeException {
	public WorkflowVersionNotFoundException() {
		super("Workflow version not found");
	}
}
