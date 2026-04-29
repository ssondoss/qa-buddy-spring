package com.sondos.qa_buddy.workflows.environments.exceptions;

public class EnvironmentNotFoundException extends RuntimeException {
	public EnvironmentNotFoundException() {
		super("Environment not found");
	}
}
