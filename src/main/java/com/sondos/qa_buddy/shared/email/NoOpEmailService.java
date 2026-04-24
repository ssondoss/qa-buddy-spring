package com.sondos.qa_buddy.shared.email;

import com.sondos.qa_buddy.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailService implements EmailService {
	private static final Logger log = LoggerFactory.getLogger(NoOpEmailService.class);

	@Override
	public boolean sendVerificationEmail(User user, String verificationUrl) {
		log.warn("Verification email skipped because app.mail.enabled is false");
		return false;
	}
}
