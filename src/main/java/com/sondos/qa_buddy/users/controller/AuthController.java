package com.sondos.qa_buddy.users.controller;

import com.sondos.qa_buddy.security.JwtService;
import com.sondos.qa_buddy.security.JwtToken;
import com.sondos.qa_buddy.shared.api.ApiResponse;
import com.sondos.qa_buddy.users.dto.AuthResponse;
import com.sondos.qa_buddy.users.dto.LoginRequest;
import com.sondos.qa_buddy.users.dto.RegisterResponse;
import com.sondos.qa_buddy.users.dto.RegisterRequest;
import com.sondos.qa_buddy.users.dto.ResendVerificationRequest;
import com.sondos.qa_buddy.users.dto.ResendVerificationResponse;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.exception.InvalidVerificationTokenException;
import com.sondos.qa_buddy.users.mapper.AuthMapper;
import com.sondos.qa_buddy.users.service.EmailVerificationService;
import com.sondos.qa_buddy.users.service.RegistrationResult;
import com.sondos.qa_buddy.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthMapper authMapper;
	private final UserService userService;
	private final EmailVerificationService emailVerificationService;
	private final JwtService jwtService;

	@Value("${app.frontend.login-url:http://localhost:4200/login}")
	private String frontendLoginUrl;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
		RegistrationResult result = userService.register(request.email(), request.password());
		String message = result.verificationEmailSent()
				? "Registration successful. Check your email to verify your account."
				: "Registration successful, but the verification email could not be sent.";
		RegisterResponse response = new RegisterResponse(
				result.user().getId(),
				result.user().getEmail(),
				true,
				result.verificationEmailSent(),
				message
		);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), "Registration successful", response));
	}

	@PostMapping("/login")
	public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		User user = userService.authenticate(request.email(), request.password());
		JwtToken token = jwtService.createAccessToken(user.getId(), user.getEmail());
		return ApiResponse.success(HttpStatus.OK.value(), "Login successful", authMapper.toAuthResponse(user, token));
	}

	@PostMapping("/resend-verification")
	public ApiResponse<ResendVerificationResponse> resendVerification(
			@Valid @RequestBody ResendVerificationRequest request
	) {
		boolean verificationEmailSent = emailVerificationService.resendVerification(request.email());
		String message = "If the email exists and is not verified, a verification email has been sent.";
		ResendVerificationResponse response = new ResendVerificationResponse(verificationEmailSent, message);
		return ApiResponse.success(HttpStatus.OK.value(), "Resend verification request processed", response);
	}

	@GetMapping("/verify-email")
	public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
		String redirectUrl = frontendLoginUrl + "?verified=true";

		try {
			emailVerificationService.verifyToken(token);
		} catch (InvalidVerificationTokenException ex) {
			redirectUrl = frontendLoginUrl + "?verified=false";
		}

		return ResponseEntity.status(HttpStatus.FOUND)
				.header("Location", redirectUrl)
				.build();
	}
}
