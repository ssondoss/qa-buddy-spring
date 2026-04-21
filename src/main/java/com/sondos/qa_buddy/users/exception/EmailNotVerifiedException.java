package com.sondos.qa_buddy.users.exception;

public class EmailNotVerifiedException extends RuntimeException {
	public EmailNotVerifiedException() {
		super("Email address is not verified");
	}
}
