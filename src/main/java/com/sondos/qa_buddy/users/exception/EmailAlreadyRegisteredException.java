package com.sondos.qa_buddy.users.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException() {
		super("Email already registered");
	}
}

