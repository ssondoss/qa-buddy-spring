package com.sondos.qa_buddy.workflows.environments.exceptions;

public class EnvironmentAlreadyExistsException extends RuntimeException {
	public EnvironmentAlreadyExistsException() {
		super("Environment already exists");
	}
}
