package com.sondos.qa_buddy.users.controller;

import com.sondos.qa_buddy.security.JwtService;
import com.sondos.qa_buddy.security.JwtToken;
import com.sondos.qa_buddy.users.dto.AuthResponse;
import com.sondos.qa_buddy.users.dto.LoginRequest;
import com.sondos.qa_buddy.users.dto.RegisterRequest;
import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
	private final UserService userService;
	private final JwtService jwtService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		User user = userService.register(request.email(), request.password());
		JwtToken token = jwtService.createAccessToken(user.getId(), user.getEmail());

		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user, token));
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		User user = userService.authenticate(request.email(), request.password());
		JwtToken token = jwtService.createAccessToken(user.getId(), user.getEmail());
		return toResponse(user, token);
	}

	private static AuthResponse toResponse(User user, JwtToken token) {
		return new AuthResponse(
				token.value(),
				"Bearer",
				token.expiresAt(),
				user.getId(),
				user.getEmail()
		);
	}
}

