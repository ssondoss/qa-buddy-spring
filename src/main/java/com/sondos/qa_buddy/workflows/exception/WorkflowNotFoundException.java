package com.sondos.qa_buddy.workflows.exception;

public class WorkflowNotFoundException extends RuntimeException {
	public WorkflowNotFoundException() {
		super("Workflow not found");
	}
}
