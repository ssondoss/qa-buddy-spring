package com.sondos.qa_buddy.users.application;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException() {
		super("Email already registered");
	}
}

