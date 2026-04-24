package com.sondos.qa_buddy.shared.email;

import com.sondos.qa_buddy.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpEmailService implements EmailService {
	private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

	private final JavaMailSender mailSender;
	private final boolean mailEnabled;
	private final String fromAddress;

	public SmtpEmailService(
			JavaMailSender mailSender,
			@Value("${app.mail.enabled:false}") boolean mailEnabled,
			@Value("${app.mail.from:}") String fromAddress
	) {
		this.mailSender = mailSender;
		this.mailEnabled = mailEnabled;
		this.fromAddress = fromAddress;
	}

	@Override
	public boolean sendVerificationEmail(User user, String verificationUrl) {
		if (!mailEnabled) {
			log.warn("Verification email skipped because app.mail.enabled is false");
			return false;
		}

		if (fromAddress == null || fromAddress.isBlank()) {
			log.warn("Verification email skipped because app.mail.from is not configured");
			return false;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(user.getEmail());
		message.setSubject("Verify your QA Buddy account");
		message.setText("""
				Hi,

				Thanks for registering with QA Buddy.

				Verify your email address by opening this link:
				%s
				""");
		message.setText(message.getText().formatted(verificationUrl));

		try {
			mailSender.send(message);
			log.info("Verification email sent to {}", user.getEmail());
			return true;
		} catch (MailException ex) {
			log.warn("Failed to send verification email to {}", user.getEmail(), ex);
			return false;
		}
	}
}
