package com.sondos.qa_buddy.workflows.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.repository.UserRepository;
import com.sondos.qa_buddy.workflows.client.RecordingStartRequest;
import com.sondos.qa_buddy.workflows.client.RecordingNodeClient;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;
import com.sondos.qa_buddy.workflows.environments.repository.EnvironmentRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowStepRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private WorkflowVersionRepository workflowVersionRepository;

	@Autowired
	private WorkflowStepRepository workflowStepRepository;

	@Autowired
	private EnvironmentRepository environmentRepository;

	@Value("${app.recording.shared-secret:qa-buddy-recording-shared-secret}")
	private String recordingSecret;

	@MockitoBean
	private RecordingNodeClient recordingNodeClient;

	@Test
	void createsWorkflowStartsRecordingAndCompletesIt() throws Exception {
		UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		User user = User.builder()
				.id(userId)
				.email("workflow@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		mockMvc.perform(post("/api/workflows")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Login workflow",
								  "description": "Manual workflow"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("Workflow created"))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.stepCount").value(0))
				.andExpect(jsonPath("$.data.versionStatus").value("DRAFT"));

		Workflow workflow = workflowRepository.findAll().getFirst();
		WorkflowVersion version1 = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId()).getFirst();
		List<WorkflowStep> version1Steps = workflowStepRepository.findByWorkflowVersionIdOrderByOrderIndexAsc(version1.getId());
		assertThat(version1Steps).isEmpty();
		assertThat(version1.getStatus().name()).isEqualTo("DRAFT");

		Environment environment = environmentRepository.save(
				Environment.builder()
						.id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
						.createdBy(user)
						.name("Staging")
						.webBaseUrl("https://example.com/")
						.apiBaseUrl(null)
						.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
						.updatedAt(Instant.parse("2026-04-24T00:00:00Z"))
						.build()
		);

		mockMvc.perform(post("/api/workflows/{workflowId}/versions/start", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "environmentId": "22222222-2222-2222-2222-222222222222",
								  "startPath": "/login"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Recording started"))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.stepCount").value(0))
				.andExpect(jsonPath("$.data.versionStatus").value("RECORDING"));

		WorkflowVersion versionAfterStart = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId()).getFirst();
		assertThat(versionAfterStart.getStatus().name()).isEqualTo("RECORDING");
		assertThat(versionAfterStart.getEnvironmentNameSnapshot()).isEqualTo("Staging");
		assertThat(versionAfterStart.getEnvironmentWebBaseUrlSnapshot()).isEqualTo("https://example.com/");
		assertThat(versionAfterStart.getEnvironmentApiBaseUrlSnapshot()).isNull();

		var startRequestCaptor = org.mockito.ArgumentCaptor.forClass(RecordingStartRequest.class);
		verify(recordingNodeClient).startRecording(startRequestCaptor.capture());
		RecordingStartRequest startRequest = startRequestCaptor.getValue();
		assertThat(startRequest.workflowId()).isEqualTo(workflow.getId());
		assertThat(startRequest.workflowVersionId()).isEqualTo(versionAfterStart.getId());
		assertThat(startRequest.versionNumber()).isEqualTo(1);
		assertThat(startRequest.workflowName()).isEqualTo("Login workflow");
		assertThat(startRequest.workflowDescription()).isEqualTo("Manual workflow");
		assertThat(startRequest.webBaseUrl()).isEqualTo("https://example.com");
		assertThat(startRequest.apiBaseUrl()).isNull();
		assertThat(startRequest.startPath()).isEqualTo("/login");

		version1 = versionAfterStart;

		mockMvc.perform(post("/api/workflow-versions/{workflowVersionId}/complete", version1.getId())
						.header("X-Integration-Secret", recordingSecret)
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "status": "READY",
								  "steps": [
								    {
								      "action": "NAVIGATE",
								      "value": "https://example.com/login"
								    },
								    {
								      "action": "FILL",
								      "selector": "#email",
								      "value": "user@example.com"
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Recording completed"))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.stepCount").value(2))
				.andExpect(jsonPath("$.data.versionStatus").value("READY"));

		List<WorkflowVersion> versions = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId());
		assertThat(versions).hasSize(1);
		assertThat(versions.getFirst().getStatus().name()).isEqualTo("READY");

		List<WorkflowStep> savedSteps = workflowStepRepository.findByWorkflowVersionIdOrderByOrderIndexAsc(
				versions.getFirst().getId()
		);
		assertThat(savedSteps).hasSize(2);
		assertThat(savedSteps.get(0).getSource()).isEqualTo(WorkflowStepSource.RECORDED);
		assertThat(workflowRepository.findById(workflow.getId())).isPresent();
		assertThat(workflowRepository.findById(workflow.getId()).get().getCurrentVersion()).isEqualTo(1);

		mockMvc.perform(put("/api/workflows/{workflowId}", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Login workflow renamed",
								  "description": "Updated workflow"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflow updated"))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.versionStatus").value("READY"));

		mockMvc.perform(get("/api/workflows")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflows fetched"))
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].workflowId").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data[0].currentVersion").value(1))
				.andExpect(jsonPath("$.data[0].currentVersionStatus").value("READY"));

		mockMvc.perform(get("/api/workflows/{workflowId}", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflow fetched"))
				.andExpect(jsonPath("$.data.workflowId").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.versionStatus").value("READY"))
				.andExpect(jsonPath("$.data.steps.length()").value(2));
	}
}
