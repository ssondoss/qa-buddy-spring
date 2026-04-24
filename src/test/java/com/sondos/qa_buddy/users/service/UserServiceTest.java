package com.sondos.qa_buddy.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.sondos.qa_buddy.users.entity.User;
import com.sondos.qa_buddy.users.exception.EmailAlreadyRegisteredException;
import com.sondos.qa_buddy.users.exception.EmailNotVerifiedException;
import com.sondos.qa_buddy.users.exception.InvalidCredentialsException;
import com.sondos.qa_buddy.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private EmailVerificationService emailVerificationService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Test
	void registerNormalizesEmailAndSendsVerification() {
		when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(emailVerificationService.createAndSendVerification(any(User.class))).thenReturn(true);

		RegistrationResult result = userService.register("  Test@Example.COM  ", "password123");

		assertThat(result.verificationEmailSent()).isTrue();
		assertThat(result.user().getEmail()).isEqualTo("test@example.com");
		assertThat(result.user().getPasswordHash()).isEqualTo("encoded-password");
		assertThat(result.user().isEmailVerified()).isFalse();

		ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(savedUser.capture());
		assertThat(savedUser.getValue().getEmail()).isEqualTo("test@example.com");

		verify(emailVerificationService).createAndSendVerification(savedUser.getValue());
	}

	@Test
	void registerRejectsDuplicateEmail() {
		when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

		assertThatThrownBy(() -> userService.register("test@example.com", "password123"))
				.isInstanceOf(EmailAlreadyRegisteredException.class)
				.hasMessage("Email already registered");

		verify(userRepository, never()).save(any(User.class));
		verify(emailVerificationService, never()).createAndSendVerification(any(User.class));
	}

	@Test
	void authenticateRejectsMissingUser() {
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.authenticate("missing@example.com", "password123"))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("Invalid email or password");

		verify(passwordEncoder, never()).matches(any(), any());
	}

	@Test
	void authenticateRejectsWrongPassword() {
		User user = verifiedUser();
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> userService.authenticate("test@example.com", "wrong-password"))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("Invalid email or password");
	}

	@Test
	void authenticateRejectsUnverifiedUser() {
		User user = User.builder()
				.id(UUID.randomUUID())
				.email("test@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.now())
				.emailVerified(false)
				.emailVerifiedAt(null)
				.build();

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

		assertThatThrownBy(() -> userService.authenticate("test@example.com", "password123"))
				.isInstanceOf(EmailNotVerifiedException.class)
				.hasMessage("Email address is not verified");
	}

	@Test
	void authenticateReturnsVerifiedUser() {
		User user = verifiedUser();
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

		User authenticatedUser = userService.authenticate(" Test@Example.COM ", "password123");

		assertThat(authenticatedUser).isSameAs(user);
	}

	private static User verifiedUser() {
		return User.builder()
				.id(UUID.randomUUID())
				.email("test@example.com")
				.passwordHash("encoded-password")
				.createdAt(Instant.now())
				.emailVerified(true)
				.emailVerifiedAt(Instant.now())
				.build();
	}
}
