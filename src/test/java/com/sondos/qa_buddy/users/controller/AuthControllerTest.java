package com.sondos.qa_buddy.users.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sondos.qa_buddy.users.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmailVerificationTokenRepository emailVerificationTokenRepository;

	@Test
	void registerRequiresVerificationBeforeLogin() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com","password":"password123"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value("Registration successful"))
				.andExpect(jsonPath("$.data.verificationRequired").value(true))
				.andExpect(jsonPath("$.data.verificationEmailSent").value(false))
				.andExpect(jsonPath("$.data.message").value("Registration successful, but the verification email could not be sent."))
				.andExpect(jsonPath("$.data.userId").isNotEmpty())
				.andExpect(jsonPath("$.data.email").value("test@example.com"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com","password":"password123"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Email address is not verified"));

		mockMvc.perform(post("/api/auth/resend-verification")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Resend verification request processed"))
				.andExpect(jsonPath("$.data.verificationEmailSent").value(false))
				.andExpect(jsonPath("$.data.message").value("If the email exists and is not verified, a verification email has been sent."));

		String token = emailVerificationTokenRepository.findAll().getFirst().getToken();

		mockMvc.perform(get("/api/auth/verify-email")
						.param("token", token))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "http://localhost:4200/login?verified=true"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(APPLICATION_JSON)
						.content("""
								{"email":"test@example.com","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"));

		mockMvc.perform(get("/api/auth/verify-email")
						.param("token", "invalid-token"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "http://localhost:4200/login?verified=false"));
	}
}

