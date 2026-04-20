package com.sondos.qa_buddy.users.exception;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super("Invalid email or password");
	}
}

