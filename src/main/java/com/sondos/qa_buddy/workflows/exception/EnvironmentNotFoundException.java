package com.sondos.qa_buddy.workflows.exception;

public class EnvironmentNotFoundException extends RuntimeException {
	public EnvironmentNotFoundException() {
		super("Environment not found");
	}
}
