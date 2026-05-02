package com.sondos.qa_buddy.workflows.versions.exception;

public class VersionLockedException extends RuntimeException {
	public VersionLockedException() {
		super("Version is recording and cannot be modified");
	}
}
