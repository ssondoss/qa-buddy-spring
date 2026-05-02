package com.sondos.qa_buddy.workflows.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.repository.UserRepository;
import com.sondos.qa_buddy.workflows.entity.Workflow;
import com.sondos.qa_buddy.workflows.repository.WorkflowRepository;
import com.sondos.qa_buddy.workflows.versions.client.RecordingNodeClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VersionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WorkflowRepository workflowRepository;

	@MockitoBean
	private RecordingNodeClient recordingNodeClient;

	@Test
	void getsVersionsByWorkflowId() throws Exception {
		UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		User user = User.builder()
				.id(userId)
				.email("versions@example.com")
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
								  "name": "Versions workflow",
								  "description": "Workflow for version listing"
								}
								"""))
				.andExpect(status().isCreated());

		Workflow workflow = workflowRepository.findAll().stream()
				.filter(candidate -> "Versions workflow".equals(candidate.getName()))
				.findFirst()
				.orElseThrow();

		mockMvc.perform(get("/api/workflows/{workflowId}/versions", workflow.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Versions fetched"))
				.andExpect(jsonPath("$.data.length()").value(0));
	}
}
