package com.sondos.qa_buddy.workflows.executions.service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sondos.qa_buddy.users.repository.UserRepository;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionLaunchContext;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionRunNodeResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStepRequest;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStepRunResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionView;
import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import com.sondos.qa_buddy.workflows.executions.entity.ExecutionStepResult;
import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStatus;
import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStepStatus;
import com.sondos.qa_buddy.workflows.executions.factory.ExecutionResponseFactory;
import com.sondos.qa_buddy.workflows.executions.respository.ExecutionRepository;
import com.sondos.qa_buddy.workflows.executions.respository.ExecutionStepResultRepository;
import com.sondos.qa_buddy.workflows.exception.WorkflowNotFoundException;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import com.sondos.qa_buddy.workflows.versions.exception.VersionNotFoundException;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import com.sondos.qa_buddy.workflows.executions.exception.VersionNotReadyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExecutionPersistenceService {
	private final UserRepository userRepository;
	private final WorkflowRepository workflowRepository;
	private final VersionRepository versionRepository;
	private final StepRepository stepRepository;
	private final ExecutionRepository executionRepository;
	private final ExecutionStepResultRepository executionStepResultRepository;
	private final ExecutionResponseFactory executionResponseFactory;

	@Transactional(readOnly = true)
	public ExecutionLaunchContext prepareLaunch(UUID userId, UUID workflowId, UUID versionId) {
		Workflow workflow = workflowRepository.findByIdAndDeletedAtIsNull(workflowId)
				.orElseThrow(WorkflowNotFoundException::new);
		Version version = versionRepository.findById(versionId)
				.orElseThrow(VersionNotFoundException::new);

		if (!workflow.getId().equals(version.getWorkflow().getId())) {
			throw new VersionNotFoundException();
		}

		if (version.getStatus() != VersionStatus.READY
				|| version.getEnvironmentNameSnapshot() == null
				|| version.getEnvironmentWebBaseUrlSnapshot() == null) {
			throw new VersionNotReadyException();
		}

		List<ExecutionStepRequest> steps = stepRepository.findByVersionIdOrderByOrderIndexAsc(versionId)
				.stream()
				.map(this::toStepRequest)
				.toList();

		return new ExecutionLaunchContext(
				userId,
				workflow.getId(),
				workflow.getName(),
				workflow.getDescription(),
				version.getId(),
				version.getVersionNumber(),
				version.getEnvironmentNameSnapshot(),
				version.getEnvironmentWebBaseUrlSnapshot(),
				version.getEnvironmentApiBaseUrlSnapshot(),
				steps
		);
	}

	@Transactional
	public Execution createRunningExecution(ExecutionLaunchContext context, Instant startedAt) {
		Execution execution = Execution.builder()
				.id(UUID.randomUUID())
				.workflow(workflowRepository.getReferenceById(context.workflowId()))
				.version(versionRepository.getReferenceById(context.versionId()))
				.status(ExecutionStatus.RUNNING)
				.startedAt(startedAt)
				.triggeredBy(userRepository.getReferenceById(context.userId()))
				.build();
		execution.captureEnvironmentSnapshot(
				context.environmentNameSnapshot(),
				context.environmentWebBaseUrlSnapshot(),
				context.environmentApiBaseUrlSnapshot()
		);
		return executionRepository.save(execution);
	}

	@Transactional
	public ExecutionView finalizeExecution(
			UUID executionId,
			ExecutionLaunchContext context,
			ExecutionRunNodeResponse nodeResponse
	) {
		Execution execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found"));

		Instant startedAt = nodeResponse.startedAt() == null ? execution.getStartedAt() : nodeResponse.startedAt();
		Instant finishedAt = nodeResponse.finishedAt() == null ? Instant.now() : nodeResponse.finishedAt();

		Map<UUID, ExecutionStepRunResponse> responseByStepId = (nodeResponse.stepResults() == null
				? List.<ExecutionStepRunResponse>of()
				: nodeResponse.stepResults())
				.stream()
				.filter(stepResponse -> stepResponse.stepId() != null)
				.collect(Collectors.toMap(
						ExecutionStepRunResponse::stepId,
						stepResponse -> stepResponse,
						(left, right) -> right,
						LinkedHashMap::new
				));

		ExecutionStatus finalStatus = nodeResponse.status() == null
				? inferExecutionStatus(context.steps(), responseByStepId)
				: nodeResponse.status();

		execution.updateOutcome(finalStatus, startedAt, finishedAt);
		executionRepository.save(execution);

		List<ExecutionStepResult> stepResults = context.steps().stream()
				.map(step -> buildStepResult(execution, step, responseByStepId.get(step.stepId()), startedAt, finishedAt))
				.toList();
		executionStepResultRepository.saveAll(stepResults);

		List<ExecutionStepResult> savedStepResults = executionStepResultRepository.findByExecutionIdOrderByOrderIndexAsc(
				executionId
		);
		return executionResponseFactory.toView(execution, savedStepResults);
	}

	@Transactional
	public ExecutionView markFailed(UUID executionId) {
		Execution execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found"));
		execution.markFailed(Instant.now());
		executionRepository.save(execution);
		List<ExecutionStepResult> savedStepResults = executionStepResultRepository.findByExecutionIdOrderByOrderIndexAsc(
				executionId
		);
		return executionResponseFactory.toView(execution, savedStepResults);
	}

	private ExecutionStepRequest toStepRequest(Step step) {
		return new ExecutionStepRequest(
				step.getId(),
				step.getOrderIndex(),
				step.getAction(),
				step.getSource(),
				step.getSelector(),
				step.getValue(),
				step.getExpectedValue(),
				step.getMeta(),
				step.isOptional(),
				step.getTimeoutMs(),
				step.getRetryCount()
		);
	}

	private ExecutionStepResult buildStepResult(
			Execution execution,
			ExecutionStepRequest step,
			ExecutionStepRunResponse stepResponse,
			Instant startedAt,
			Instant finishedAt
	) {
		Instant stepStartedAt = stepResponse == null || stepResponse.startedAt() == null
				? startedAt
				: stepResponse.startedAt();
		Instant stepFinishedAt = stepResponse == null || stepResponse.finishedAt() == null
				? finishedAt
				: stepResponse.finishedAt();
		Long durationMs = stepResponse == null || stepResponse.durationMs() == null
				? durationBetween(stepStartedAt, stepFinishedAt)
				: stepResponse.durationMs();

		return ExecutionStepResult.builder()
				.id(UUID.randomUUID())
				.execution(execution)
				.step(stepRepository.getReferenceById(step.stepId()))
				.orderIndex(step.orderIndex())
				.status(stepResponse == null
						? ExecutionStepStatus.SKIPPED
						: stepResponse.status() == null ? ExecutionStepStatus.FAILED : stepResponse.status())
				.errorMessage(stepResponse == null ? null : stepResponse.errorMessage())
				.screenshotUrl(stepResponse == null ? null : stepResponse.screenshotUrl())
				.log(stepResponse == null ? null : stepResponse.log())
				.startedAt(stepStartedAt)
				.finishedAt(stepFinishedAt)
				.durationMs(durationMs)
				.stepSnapshot(buildStepSnapshot(step))
				.build();
	}

	private Map<String, Object> buildStepSnapshot(ExecutionStepRequest step) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("stepId", step.stepId());
		snapshot.put("orderIndex", step.orderIndex());
		snapshot.put("action", step.action());
		snapshot.put("source", step.source());
		snapshot.put("selector", step.selector());
		snapshot.put("value", step.value());
		snapshot.put("expectedValue", step.expectedValue());
		snapshot.put("meta", step.meta() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(step.meta()));
		snapshot.put("optional", step.optional());
		snapshot.put("timeoutMs", step.timeoutMs());
		snapshot.put("retryCount", step.retryCount());
		return snapshot;
	}

	private ExecutionStatus inferExecutionStatus(
			List<ExecutionStepRequest> steps,
			Map<UUID, ExecutionStepRunResponse> responseByStepId
	) {
		boolean missingSteps = responseByStepId.size() < steps.size();
		boolean failedSteps = responseByStepId.values().stream()
				.anyMatch(stepResponse -> stepResponse.status() == null
						|| stepResponse.status() == ExecutionStepStatus.FAILED);

		return missingSteps || failedSteps ? ExecutionStatus.FAILED : ExecutionStatus.PASSED;
	}

	private Long durationBetween(Instant startedAt, Instant finishedAt) {
		if (startedAt == null || finishedAt == null) {
			return null;
		}

		return Duration.between(startedAt, finishedAt).toMillis();
	}
}
