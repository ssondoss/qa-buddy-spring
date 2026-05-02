package com.sondos.qa_buddy.workflows.executions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.repository.UserRepository;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.executions.client.ExecutionNodeClient;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionRunNodeResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStepRunResponse;
import com.sondos.qa_buddy.workflows.executions.entity.Execution;
import com.sondos.qa_buddy.workflows.executions.entity.ExecutionStepResult;
import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStatus;
import com.sondos.qa_buddy.workflows.executions.enums.ExecutionStepStatus;
import com.sondos.qa_buddy.workflows.executions.respository.ExecutionRepository;
import com.sondos.qa_buddy.workflows.executions.respository.ExecutionStepResultRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.enums.StepAction;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import com.sondos.qa_buddy.workflows.versions.enums.VersionStatus;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExecutionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private VersionRepository versionRepository;

	@Autowired
	private StepRepository stepRepository;

	@Autowired
	private ExecutionRepository executionRepository;

	@Autowired
	private ExecutionStepResultRepository executionStepResultRepository;

	@MockitoBean
	private ExecutionNodeClient executionNodeClient;

	@Test
	void executesReadyVersionThroughNodeClient() throws Exception {
		UUID userId = UUID.fromString("77777777-7777-7777-7777-777777777777");
		User user = User.builder()
				.id(userId)
				.email("execution@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		Workflow workflow = workflowRepository.save(Workflow.builder()
				.id(UUID.fromString("88888888-8888-8888-8888-888888888888"))
				.name("Execution workflow")
				.description("Workflow to execute")
				.createdBy(user)
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		Version version = versionRepository.save(Version.builder()
				.id(UUID.fromString("99999999-9999-9999-9999-999999999999"))
				.workflow(workflow)
				.versionNumber(1)
				.status(VersionStatus.READY)
				.createdBy(user)
				.environmentNameSnapshot("Staging")
				.environmentWebBaseUrlSnapshot("https://example.com")
				.environmentApiBaseUrlSnapshot("https://api.example.com")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		Step step1 = stepRepository.save(Step.builder()
				.id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
				.version(version)
				.createdBy(user)
				.orderIndex(1)
				.action(StepAction.CLICK)
				.source(StepSource.RECORDED)
				.selector("#login")
				.value(null)
				.expectedValue(null)
				.meta(Map.of("label", "Login"))
				.optional(false)
				.timeoutMs(5000)
				.retryCount(0)
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		Step step2 = stepRepository.save(Step.builder()
				.id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
				.version(version)
				.createdBy(user)
				.orderIndex(2)
				.action(StepAction.FILL)
				.source(StepSource.RECORDED)
				.selector("#email")
				.value("user@example.com")
				.expectedValue(null)
				.meta(Map.of("placeholder", "Email"))
				.optional(false)
				.timeoutMs(5000)
				.retryCount(1)
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		when(executionNodeClient.executeVersion(any())).thenReturn(
				new ExecutionRunNodeResponse(
						ExecutionStatus.PASSED,
						Instant.parse("2026-04-25T12:00:00Z"),
						Instant.parse("2026-04-25T12:01:00Z"),
						null,
						"Execution finished",
						List.of(
								new ExecutionStepRunResponse(
										step1.getId(),
										1,
										ExecutionStepStatus.PASSED,
										null,
										null,
										"clicked login",
										Instant.parse("2026-04-25T12:00:00Z"),
										Instant.parse("2026-04-25T12:00:10Z"),
										10_000L
								),
								new ExecutionStepRunResponse(
										step2.getId(),
										2,
										ExecutionStepStatus.PASSED,
										null,
										null,
										"filled email",
										Instant.parse("2026-04-25T12:00:10Z"),
										Instant.parse("2026-04-25T12:00:20Z"),
										10_000L
								)
						)
				)
		);

		mockMvc.perform(post("/api/workflows/{workflowId}/versions/{versionId}/execute", workflow.getId(), version.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Version execution started"))
				.andExpect(jsonPath("$.data.id").isNotEmpty())
				.andExpect(jsonPath("$.data.workflowId").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.versionId").value(version.getId().toString()))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.status").value("PASSED"))
				.andExpect(jsonPath("$.data.stepResults.length()").value(2))
				.andExpect(jsonPath("$.data.stepResults[0].status").value("PASSED"))
				.andExpect(jsonPath("$.data.stepResults[1].status").value("PASSED"));

		List<Execution> executions = executionRepository.findByWorkflowIdOrderByStartedAtDesc(workflow.getId());
		assertThat(executions).hasSize(1);
		assertThat(executions.getFirst().getStatus()).isEqualTo(ExecutionStatus.PASSED);

		List<ExecutionStepResult> stepResults = executionStepResultRepository.findByExecutionIdOrderByOrderIndexAsc(
				executions.getFirst().getId()
		);
		assertThat(stepResults).hasSize(2);
		assertThat(stepResults.getFirst().getStatus()).isEqualTo(ExecutionStepStatus.PASSED);
		assertThat(String.valueOf(stepResults.getFirst().getStepSnapshot().get("stepId")))
				.isEqualTo(step1.getId().toString());

		var executionRequestCaptor = org.mockito.ArgumentCaptor.forClass(
				com.sondos.qa_buddy.workflows.executions.dto.ExecutionStartNodeRequest.class
		);
		verify(executionNodeClient).executeVersion(executionRequestCaptor.capture());
		com.sondos.qa_buddy.workflows.executions.dto.ExecutionStartNodeRequest request = executionRequestCaptor.getValue();
		assertThat(request.executionId()).isNotNull();
		assertThat(request.workflowId()).isEqualTo(workflow.getId());
		assertThat(request.workflowVersionId()).isEqualTo(version.getId());
		assertThat(request.workflowName()).isEqualTo("Execution workflow");
		assertThat(request.environmentName()).isEqualTo("Staging");
		assertThat(request.webBaseUrl()).isEqualTo("https://example.com");
		assertThat(request.apiBaseUrl()).isEqualTo("https://api.example.com");
		assertThat(request.steps()).hasSize(2);
		assertThat(request.steps().getFirst().stepId()).isEqualTo(step1.getId());
		assertThat(request.steps().getFirst().action()).isEqualTo(StepAction.CLICK);
		assertThat(request.steps().getFirst().selector()).isEqualTo("#login");
		assertThat(request.steps().get(1).stepId()).isEqualTo(step2.getId());
		assertThat(request.steps().get(1).orderIndex()).isEqualTo(2);
		assertThat(request.steps().get(1).value()).isEqualTo("user@example.com");
	}

	@Test
	void rejectsVersionsThatAreNotReady() throws Exception {
		UUID userId = UUID.fromString("aaaaaaaa-1111-2222-3333-bbbbbbbbbbbb");
		User user = User.builder()
				.id(userId)
				.email("not-ready@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		Workflow workflow = workflowRepository.save(Workflow.builder()
				.id(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
				.name("Draft workflow")
				.description("Not ready to run")
				.createdBy(user)
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		Version version = versionRepository.save(Version.builder()
				.id(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))
				.workflow(workflow)
				.versionNumber(1)
				.status(VersionStatus.DRAFT)
				.createdBy(user)
				.environmentNameSnapshot("Staging")
				.environmentWebBaseUrlSnapshot("https://example.com")
				.environmentApiBaseUrlSnapshot(null)
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build());

		mockMvc.perform(post("/api/workflows/{workflowId}/versions/{versionId}/execute", workflow.getId(), version.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("Workflow version is not ready for execution"));

		verifyNoInteractions(executionNodeClient);
	}
}
