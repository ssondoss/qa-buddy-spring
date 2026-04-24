package com.sondos.qa_buddy.security;

public class AuthenticatedUserNotFoundException extends RuntimeException {
	public AuthenticatedUserNotFoundException() {
		super("Authenticated user not found");
	}
}
