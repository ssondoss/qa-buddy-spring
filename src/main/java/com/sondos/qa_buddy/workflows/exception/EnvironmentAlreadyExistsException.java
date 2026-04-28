package com.sondos.qa_buddy.workflows.exception;

public class EnvironmentAlreadyExistsException extends RuntimeException {
	public EnvironmentAlreadyExistsException() {
		super("Environment already exists");
	}
}
