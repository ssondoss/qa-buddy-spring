package com.sondos.qa_buddy.workflows.versions.factory;

import com.sondos.qa_buddy.workflows.versions.dto.VersionView;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import org.springframework.stereotype.Component;

@Component
public class VersionResponseFactory {
	public VersionView toView(Version version) {
		return new VersionView(
				version.getWorkflow().getId(),
				version.getId(),
				version.getVersionNumber(),
				version.getStatus(),
				version.getEnvironmentNameSnapshot(),
				version.getEnvironmentWebBaseUrlSnapshot(),
				version.getEnvironmentApiBaseUrlSnapshot(),
				version.getCreatedAt(),
				version.getUpdatedAt()
		);
	}
}
