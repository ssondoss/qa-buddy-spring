package com.sondos.qa_buddy.workflows.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.repository.UserRepository;
import com.sondos.qa_buddy.workflows.environments.entity.Environment;
import com.sondos.qa_buddy.workflows.environments.repository.EnvironmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EnvironmentControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EnvironmentRepository environmentRepository;

	@Test
	void managesEnvironmentCrud() throws Exception {
		UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		User user = User.builder()
				.id(userId)
				.email("env@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		mockMvc.perform(post("/api/environments")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Staging",
								  "webBaseUrl": "https://staging.example.com/",
								  "apiBaseUrl": "https://api.staging.example.com/"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("Environment created"))
				.andExpect(jsonPath("$.data.name").value("Staging"))
				.andExpect(jsonPath("$.data.webBaseUrl").value("https://staging.example.com"))
				.andExpect(jsonPath("$.data.apiBaseUrl").value("https://api.staging.example.com"));

		Environment environment = environmentRepository.findByCreatedByIdAndDeletedAtIsNullOrderByNameAsc(userId).getFirst();

		mockMvc.perform(get("/api/environments")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Environments fetched"))
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].name").value("Staging"));

		mockMvc.perform(get("/api/environments/{environmentId}", environment.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Environment fetched"))
				.andExpect(jsonPath("$.data.name").value("Staging"));

		mockMvc.perform(put("/api/environments/{environmentId}", environment.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Staging Updated",
								  "webBaseUrl": "https://staging-updated.example.com/",
								  "apiBaseUrl": "https://api.staging-updated.example.com/"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Environment updated"))
				.andExpect(jsonPath("$.data.name").value("Staging Updated"))
				.andExpect(jsonPath("$.data.webBaseUrl").value("https://staging-updated.example.com"))
				.andExpect(jsonPath("$.data.apiBaseUrl").value("https://api.staging-updated.example.com"));

		mockMvc.perform(delete("/api/environments/{environmentId}", environment.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Environment deleted"));

		mockMvc.perform(get("/api/environments/{environmentId}", environment.getId())
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString()))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Environment not found"));

		assertThat(environmentRepository.findByCreatedByIdAndDeletedAtIsNullOrderByNameAsc(userId)).isEmpty();
	}

	@Test
	void rejectsRelativeBaseUrls() throws Exception {
		UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");
		User user = User.builder()
				.id(userId)
				.email("relative-env@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		mockMvc.perform(post("/api/environments")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Staging",
								  "webBaseUrl": "/staging",
								  "apiBaseUrl": "api.example.com"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation error"))
				.andExpect(jsonPath("$.errors.webBaseUrl").value("must be an absolute http or https URL"))
				.andExpect(jsonPath("$.errors.apiBaseUrl").value("must be an absolute http or https URL"));
	}

	@Test
	void allowsMissingApiBaseUrl() throws Exception {
		UUID userId = UUID.fromString("55555555-5555-5555-5555-555555555555");
		User user = User.builder()
				.id(userId)
				.email("optional-api-env@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.parse("2026-04-24T00:00:00Z"))
				.emailVerified(true)
				.emailVerifiedAt(Instant.parse("2026-04-24T00:00:00Z"))
				.build();
		userRepository.save(user);

		mockMvc.perform(post("/api/environments")
						.with(jwt().jwt(jwt -> jwt.subject(user.getEmail()).claim("uid", userId.toString())))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "name": "Staging Optional API",
								  "webBaseUrl": "https://staging.example.com/"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("Environment created"))
				.andExpect(jsonPath("$.data.name").value("Staging Optional API"))
				.andExpect(jsonPath("$.data.webBaseUrl").value("https://staging.example.com"))
				.andExpect(jsonPath("$.data.apiBaseUrl").value(nullValue()));

		Environment environment = environmentRepository.findByCreatedByIdAndDeletedAtIsNullOrderByNameAsc(userId).getFirst();
		assertThat(environment.getApiBaseUrl()).isNull();
	}
}
