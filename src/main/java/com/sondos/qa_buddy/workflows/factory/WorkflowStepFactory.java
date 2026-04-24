package com.sondos.qa_buddy.workflows.factory;

import java.time.Instant;
import java.util.LinkedHashMap;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.dto.WorkflowStepRequest;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;
import org.springframework.stereotype.Component;

@Component
public class WorkflowStepFactory {
	public WorkflowStep create(
			WorkflowVersion version,
			User user,
			Instant now,
			int orderIndex,
			WorkflowStepRequest request
	) {
		WorkflowStepSource source = request.source() == null ? WorkflowStepSource.MANUAL : request.source();
		return WorkflowStep.builder()
				.id(java.util.UUID.randomUUID())
				.workflowVersion(version)
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
