package com.sondos.qa_buddy.workflows.versions.exception;

public class StepOrderMismatchException extends RuntimeException {
	public StepOrderMismatchException() {
		super("Step order does not match the version steps");
	}
}
