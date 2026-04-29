package com.sondos.qa_buddy.workflows.versions.exception;

public class RecordingCallbackUnauthorizedException extends RuntimeException {
	public RecordingCallbackUnauthorizedException() {
		super("Invalid recording callback secret");
	}
}
