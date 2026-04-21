package com.sondos.qa_buddy.users.exception;

public class InvalidVerificationTokenException extends RuntimeException {
	public InvalidVerificationTokenException() {
		super("Invalid or expired verification token");
	}
}
