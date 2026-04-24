package com.sondos.qa_buddy.workflows.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.entity.WorkflowStep;
import com.sondos.qa_buddy.workflows.entity.WorkflowVersion;
import com.sondos.qa_buddy.workflows.enums.WorkflowStepSource;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowStepRepository;
import com.sondos.qa_buddy.workflows.repository.WorkflowVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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

	@Test
	void createsAndEditsWorkflowWithVersionedSteps() throws Exception {
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
								  "description": "Manual workflow",
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
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("Workflow created"))
				.andExpect(jsonPath("$.data.versionNumber").value(1))
				.andExpect(jsonPath("$.data.stepCount").value(2))
				.andExpect(jsonPath("$.data.status").value("DRAFT"));

		Workflow workflow = workflowRepository.findAll().getFirst();
		WorkflowVersion version1 = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId()).getFirst();
		List<WorkflowStep> version1Steps = workflowStepRepository.findByWorkflowVersionIdOrderByOrderIndexAsc(version1.getId());
		assertThat(version1Steps).hasSize(2);
		assertThat(version1Steps.get(0).getSource()).isEqualTo(WorkflowStepSource.MANUAL);

		mockMvc.perform(put("/api/workflows/{workflowId}", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Login workflow v2",
								  "description": "Edited workflow",
								  "status": "READY",
								  "steps": [
								    {
								      "action": "NAVIGATE",
								      "value": "https://example.com/login",
								      "source": "RECORDED"
								    },
								    {
								      "action": "FILL",
								      "selector": "#email",
								      "value": "user@example.com"
								    },
								    {
								      "action": "CLICK",
								      "selector": "button[type='submit']"
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflow updated"))
				.andExpect(jsonPath("$.data.versionNumber").value(2))
				.andExpect(jsonPath("$.data.stepCount").value(3))
				.andExpect(jsonPath("$.data.status").value("READY"));

		List<WorkflowVersion> versions = workflowVersionRepository.findByWorkflowIdOrderByVersionNumberDesc(workflow.getId());
		assertThat(versions).hasSize(2);

		List<WorkflowStep> version2Steps = workflowStepRepository.findByWorkflowVersionIdOrderByOrderIndexAsc(
				versions.getFirst().getId()
		);
		assertThat(version2Steps).hasSize(3);
		assertThat(version2Steps.get(0).getSource()).isEqualTo(WorkflowStepSource.RECORDED);
		assertThat(workflowRepository.findById(workflow.getId())).isPresent();
		assertThat(workflowRepository.findById(workflow.getId()).get().getCurrentVersion()).isEqualTo(2);

		mockMvc.perform(get("/api/workflows")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflows fetched"))
				.andExpect(jsonPath("$.data.items[0].workflowId").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.items[0].currentVersion").value(2))
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.page").value(0))
				.andExpect(jsonPath("$.data.size").value(10))
				.andExpect(jsonPath("$.data.last").value(true));

		mockMvc.perform(get("/api/workflows/{workflowId}", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Workflow fetched"))
				.andExpect(jsonPath("$.data.workflowId").value(workflow.getId().toString()))
				.andExpect(jsonPath("$.data.versionNumber").value(2))
				.andExpect(jsonPath("$.data.steps.length()").value(3));
	}
}
