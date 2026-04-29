package com.sondos.qa_buddy.workflows.versions.factory;

import java.time.Instant;
import java.util.LinkedHashMap;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.versions.dto.StepRequest;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import org.springframework.stereotype.Component;

@Component
public class StepFactory {
	public Step create(
			Version version,
			User user,
			Instant now,
			int orderIndex,
			StepRequest request,
			StepSource defaultSource
	) {
		StepSource source = request.source() == null ? defaultSource : request.source();
		return Step.builder()
				.id(java.util.UUID.randomUUID())
				.version(version)
				.createdBy(user)
				.orderIndex(orderIndex)
				.action(request.action())
				.source(source)
				.selector(request.selector())
				.value(request.value())
				.expectedValue(request.expectedValue())
				.meta(request.meta() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request.meta()))
				.optional(Boolean.TRUE.equals(request.optional()))
				.timeoutMs(request.timeoutMs())
				.retryCount(request.retryCount() == null ? 0 : request.retryCount())
				.createdAt(now)
				.updatedAt(now)
				.build();
	}
}
