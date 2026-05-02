package com.sondos.qa_buddy.workflows.executions.service;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.executions.client.ExecutionNodeClient;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionLaunchContext;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionRunNodeResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStartNodeRequest;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionView;
import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionService {
	private final CurrentUserService currentUserService;
	private final ExecutionPersistenceService executionPersistenceService;
	private final ExecutionNodeClient executionNodeClient;

	public ExecutionView executeVersion(Jwt jwt, UUID workflowId, UUID versionId) {
		User user = currentUserService.getCurrentUser(jwt);
		ExecutionLaunchContext context = executionPersistenceService.prepareLaunch(user.getId(), workflowId, versionId);
		Execution execution = executionPersistenceService.createRunningExecution(context, Instant.now());
		ExecutionStartNodeRequest request = toNodeRequest(execution.getId(), context);

		try {
			ExecutionRunNodeResponse nodeResponse = executionNodeClient.executeVersion(request);
			return executionPersistenceService.finalizeExecution(execution.getId(), context, nodeResponse);
		} catch (RuntimeException ex) {
			try {
				executionPersistenceService.markFailed(execution.getId());
			} catch (RuntimeException ignored) {
				// best effort
			}
			throw ex;
		}
	}

	private ExecutionStartNodeRequest toNodeRequest(UUID executionId, ExecutionLaunchContext context) {
		return new ExecutionStartNodeRequest(
				executionId,
				context.workflowId(),
				context.versionId(),
				context.versionNumber(),
				context.workflowName(),
				context.workflowDescription(),
				context.environmentNameSnapshot(),
				context.environmentWebBaseUrlSnapshot(),
				context.environmentApiBaseUrlSnapshot(),
				context.steps()
		);
	}
}
