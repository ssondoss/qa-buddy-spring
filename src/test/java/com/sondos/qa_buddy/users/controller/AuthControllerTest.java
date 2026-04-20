package com.sondos.qa_buddy.users.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void registerThenLoginReturnsToken() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com","password":"password123"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.userId").isNotEmpty())
				.andExpect(jsonPath("$.email").value("test@example.com"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"));
	}
}

