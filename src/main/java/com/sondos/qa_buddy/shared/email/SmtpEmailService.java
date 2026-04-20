package com.sondos.qa_buddy.shared.email;

import com.sondos.qa_buddy.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
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
	public void sendRegistrationEmail(User user) {
		if (!mailEnabled) {
			return;
		}

		if (fromAddress == null || fromAddress.isBlank()) {
			log.warn("Registration email skipped because app.mail.from is not configured");
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(user.getEmail());
		message.setSubject("Welcome to QA Buddy");
		message.setText("""
				Hi,

				Your QA Buddy account has been created successfully.

				You can now log in and start using the application.
				""");

		try {
			mailSender.send(message);
		} catch (MailException ex) {
			log.warn("Failed to send registration email to {}", user.getEmail(), ex);
		}
	}
}
