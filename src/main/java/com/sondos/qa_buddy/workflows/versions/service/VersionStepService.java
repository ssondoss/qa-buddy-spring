package com.sondos.qa_buddy.workflows.versions.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sondos.qa_buddy.security.CurrentUserService;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.factory.WorkflowResponseFactory;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.dto.StepRequest;
import com.sondos.qa_buddy.workflows.versions.dto.StepReorderRequest;
import com.sondos.qa_buddy.workflows.versions.dto.StepView;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import com.sondos.qa_buddy.workflows.versions.exception.StepNotFoundException;
import com.sondos.qa_buddy.workflows.versions.exception.StepOrderMismatchException;
import com.sondos.qa_buddy.workflows.versions.exception.VersionLockedException;
import com.sondos.qa_buddy.workflows.versions.exception.VersionNotFoundException;
import com.sondos.qa_buddy.workflows.versions.factory.StepFactory;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VersionStepService {
	private final CurrentUserService currentUserService;
	private final WorkflowResponseFactory workflowResponseFactory;
	private final WorkflowRepository workflowRepository;
	private final VersionRepository versionRepository;
	private final StepRepository stepRepository;
	private final StepFactory stepFactory;

	@Transactional(readOnly = true)
	public List<StepView> getAll(UUID workflowVersionId) {
		requireVersion(workflowVersionId);
		return stepRepository.findByVersionIdOrderByOrderIndexAsc(workflowVersionId)
				.stream()
				.map(workflowResponseFactory::toStepView)
				.toList();
	}

	@Transactional
	public StepView create(Jwt jwt, UUID workflowVersionId, StepRequest request) {
		User user = currentUserService.getCurrentUser(jwt);
		Version version = requireEditableVersion(workflowVersionId);
		Workflow workflow = version.getWorkflow();
		Instant now = Instant.now();

		int nextOrderIndex = stepRepository.findByVersionIdOrderByOrderIndexAsc(workflowVersionId).size() + 1;
		Step step = stepFactory.create(version, user, now, nextOrderIndex, request, StepSource.MANUAL);
		Step savedStep = stepRepository.save(step);

		touchParent(version, workflow, now);
		return workflowResponseFactory.toStepView(savedStep);
	}

	@Transactional
	public StepView update(Jwt jwt, UUID workflowVersionId, UUID stepId, StepRequest request) {
		currentUserService.getCurrentUser(jwt);
		Version version = requireEditableVersion(workflowVersionId);
		Step step = requireStep(workflowVersionId, stepId);
		Workflow workflow = version.getWorkflow();
		Instant now = Instant.now();

		step.updateDetails(
				request.action(),
				request.source() == null ? step.getSource() : request.source(),
				request.selector() == null ? step.getSelector() : request.selector(),
				request.value() == null ? step.getValue() : request.value(),
				request.expectedValue() == null ? step.getExpectedValue() : request.expectedValue(),
				request.meta() == null ? step.getMeta() : request.meta(),
				request.optional() == null ? step.isOptional() : request.optional(),
				request.timeoutMs() == null ? step.getTimeoutMs() : request.timeoutMs(),
				request.retryCount() == null ? step.getRetryCount() : request.retryCount(),
				now
		);
		stepRepository.save(step);

		touchParent(version, workflow, now);
		return workflowResponseFactory.toStepView(step);
	}

	@Transactional
	public void delete(Jwt jwt, UUID workflowVersionId, UUID stepId) {
		currentUserService.getCurrentUser(jwt);
		Version version = requireEditableVersion(workflowVersionId);
		Step step = requireStep(workflowVersionId, stepId);
		Workflow workflow = version.getWorkflow();
		Instant now = Instant.now();

		stepRepository.delete(step);
		stepRepository.flush();

		List<Step> remainingSteps = stepRepository.findByVersionIdOrderByOrderIndexAsc(workflowVersionId);
		renumberSteps(remainingSteps, now);
		touchParent(version, workflow, now);
	}

	@Transactional
	public List<StepView> reorder(Jwt jwt, UUID workflowVersionId, StepReorderRequest request) {
		currentUserService.getCurrentUser(jwt);
		Version version = requireEditableVersion(workflowVersionId);
		Workflow workflow = version.getWorkflow();
		List<Step> currentSteps = stepRepository.findByVersionIdOrderByOrderIndexAsc(workflowVersionId);
		List<UUID> requestedStepIds = request.stepIds();

		validateReorder(currentSteps, requestedStepIds);

		Map<UUID, Step> stepById = currentSteps.stream()
				.collect(Collectors.toMap(Step::getId, Function.identity()));
		List<Step> reorderedSteps = new ArrayList<>(requestedStepIds.size());
		for (UUID stepId : requestedStepIds) {
			reorderedSteps.add(stepById.remove(stepId));
		}

		Instant now = Instant.now();
		renumberSteps(currentSteps, reorderedSteps, now);
		touchParent(version, workflow, now);

		return reorderedSteps.stream()
				.map(workflowResponseFactory::toStepView)
				.toList();
	}

	private Version requireVersion(UUID workflowVersionId) {
		return versionRepository.findById(workflowVersionId)
				.orElseThrow(VersionNotFoundException::new);
	}

	private Version requireEditableVersion(UUID workflowVersionId) {
		Version version = requireVersion(workflowVersionId);
		if (version.getStatus() == VersionStatus.RECORDING) {
			throw new VersionLockedException();
		}
		return version;
	}

	private Step requireStep(UUID workflowVersionId, UUID stepId) {
		return stepRepository.findByVersionIdAndId(workflowVersionId, stepId)
				.orElseThrow(StepNotFoundException::new);
	}

	private void validateReorder(List<Step> currentSteps, List<UUID> requestedStepIds) {
		if (requestedStepIds == null || currentSteps.size() != requestedStepIds.size()) {
			throw new StepOrderMismatchException();
		}

		if (new HashSet<>(requestedStepIds).size() != requestedStepIds.size()) {
			throw new StepOrderMismatchException();
		}

		HashSet<UUID> currentStepIds = currentSteps.stream()
				.map(Step::getId)
				.collect(Collectors.toCollection(HashSet::new));
		if (!currentStepIds.equals(new HashSet<>(requestedStepIds))) {
			throw new StepOrderMismatchException();
		}
	}

	private void renumberSteps(List<Step> steps, Instant now) {
		renumberSteps(steps, steps, now);
	}

	private void renumberSteps(List<Step> tempOrderSteps, List<Step> finalOrderSteps, Instant now) {
		int tempOrder = -1;
		for (Step step : tempOrderSteps) {
			step.updateOrder(tempOrder--, now);
		}
		stepRepository.saveAll(tempOrderSteps);
		stepRepository.flush();

		IntStream.range(0, finalOrderSteps.size())
				.forEach(index -> finalOrderSteps.get(index).updateOrder(index + 1, now));
		stepRepository.saveAll(finalOrderSteps);
		stepRepository.flush();
	}

	private void touchParent(Version version, Workflow workflow, Instant now) {
		version.touch(now);
		versionRepository.save(version);
		workflow.updateDetails(workflow.getName(), workflow.getDescription(), now);
		workflowRepository.save(workflow);
	}
}
