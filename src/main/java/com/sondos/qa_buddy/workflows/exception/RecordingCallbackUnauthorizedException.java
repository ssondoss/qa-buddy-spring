package com.sondos.qa_buddy.workflows.exception;

public class RecordingCallbackUnauthorizedException extends RuntimeException {
	public RecordingCallbackUnauthorizedException() {
		super("Invalid recording callback secret");
	}
}
