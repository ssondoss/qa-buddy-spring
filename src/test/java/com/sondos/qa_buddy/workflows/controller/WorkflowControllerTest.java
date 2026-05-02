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
import com.sondos.qa_buddy.workflows.versions.client.RecordingStartNodeRequest;
import com.sondos.qa_buddy.workflows.versions.client.RecordingNodeClient;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.versions.entity.Version;
import com.sondos.qa_buddy.workflows.versions.entity.Step;
import com.sondos.qa_buddy.workflows.versions.enums.StepSource;
import com.sondos.qa_buddy.workflows.environments.repository.EnvironmentRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.repository.StepRepository;
import com.sondos.qa_buddy.workflows.versions.repository.VersionRepository;
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
	private VersionRepository versionRepository;

	@Autowired
	private StepRepository stepRepository;

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
				.andExpect(jsonPath("$.data.id").isNotEmpty())
				.andExpect(jsonPath("$.data.name").value("Login workflow"))
				.andExpect(jsonPath("$.data.description").value("Manual workflow"))
				.andExpect(jsonPath("$.data.versionsCount").value(0))
				.andExpect(jsonPath("$.data.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

		Workflow workflow = workflowRepository.findAll().stream()
				.filter(candidate -> "Login workflow".equals(candidate.getName()))
				.findFirst()
				.orElseThrow();
		assertThat(versionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId())).isEmpty();

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
				.andExpect(jsonPath("$.data.id").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.name").value("Login workflow"))
				.andExpect(jsonPath("$.data.description").value("Manual workflow"))
				.andExpect(jsonPath("$.data.versionsCount").value(1));

		Version versionAfterStart = versionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId()).getFirst();
		assertThat(versionAfterStart.getVersionNumber()).isEqualTo(1);
		assertThat(versionAfterStart.getStatus().name()).isEqualTo("RECORDING");
		assertThat(versionAfterStart.getEnvironmentNameSnapshot()).isEqualTo("Staging");
		assertThat(versionAfterStart.getEnvironmentWebBaseUrlSnapshot()).isEqualTo("https://example.com/");
		assertThat(versionAfterStart.getEnvironmentApiBaseUrlSnapshot()).isNull();

		var startRequestCaptor = org.mockito.ArgumentCaptor.forClass(RecordingStartNodeRequest.class);
		verify(recordingNodeClient).startRecording(startRequestCaptor.capture());
		RecordingStartNodeRequest startRequest = startRequestCaptor.getValue();
		assertThat(startRequest.workflowId()).isEqualTo(workflow.getId());
		assertThat(startRequest.workflowVersionId()).isEqualTo(versionAfterStart.getId());
		assertThat(startRequest.versionNumber()).isEqualTo(1);
		assertThat(startRequest.workflowName()).isEqualTo("Login workflow");
		assertThat(startRequest.workflowDescription()).isEqualTo("Manual workflow");
		assertThat(startRequest.webBaseUrl()).isEqualTo("https://example.com");
		assertThat(startRequest.apiBaseUrl()).isNull();
		assertThat(startRequest.startPath()).isEqualTo("/login");

		Version version1 = versionAfterStart;

		mockMvc.perform(post("/api/version/recording/complete/{workflowVersionId}", version1.getId())
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
				.andExpect(jsonPath("$.data.id").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.name").value("Login workflow"))
				.andExpect(jsonPath("$.data.description").value("Manual workflow"))
				.andExpect(jsonPath("$.data.versionsCount").value(1));

		List<Version> versions = versionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId());
		assertThat(versions).hasSize(1);
		assertThat(versions.getFirst().getVersionNumber()).isEqualTo(1);
		assertThat(versions.getFirst().getStatus().name()).isEqualTo("READY");

		List<Step> savedSteps = stepRepository.findByVersionIdOrderByOrderIndexAsc(
				versions.getFirst().getId()
		);
		assertThat(savedSteps).hasSize(2);
		assertThat(savedSteps.get(0).getSource()).isEqualTo(StepSource.RECORDED);
		assertThat(workflowRepository.findById(workflow.getId())).isPresent();
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
				.andExpect(jsonPath("$.data.id").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.name").value("Login workflow renamed"))
				.andExpect(jsonPath("$.data.description").value("Updated workflow"))
				.andExpect(jsonPath("$.data.versionsCount").value(1));

		mockMvc.perform(get("/api/workflows")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflows fetched"))
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].id").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data[0].name").value("Login workflow renamed"))
				.andExpect(jsonPath("$.data[0].description").value("Updated workflow"))
				.andExpect(jsonPath("$.data[0].versionsCount").value(1))
				.andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
				.andExpect(jsonPath("$.data[0].updatedAt").isNotEmpty());
	}
}
