package com.sondos.qa_buddy.workflows.versions.exception;

public class VersionNotFoundException extends RuntimeException {
	public VersionNotFoundException() {
		super("Workflow version not found");
	}
}
