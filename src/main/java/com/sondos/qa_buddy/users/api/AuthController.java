package com.sondos.qa_buddy.users.api;

import com.sondos.qa_buddy.security.JwtService;
import com.sondos.qa_buddy.security.JwtToken;
import com.sondos.qa_buddy.users.api.dto.AuthResponse;
import com.sondos.qa_buddy.users.api.dto.LoginRequest;
import com.sondos.qa_buddy.users.api.dto.RegisterRequest;
import com.sondos.qa_buddy.users.application.UserService;
import com.sondos.qa_buddy.users.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserService userService;
	private final JwtService jwtService;

	public AuthController(UserService userService, JwtService jwtService) {
		this.userService = userService;
		this.jwtService = jwtService;
	}

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

