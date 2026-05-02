package com.sondos.qa_buddy.workflows.versions.exception;

public class StepNotFoundException extends RuntimeException {
	public StepNotFoundException() {
		super("Step not found");
	}
}
