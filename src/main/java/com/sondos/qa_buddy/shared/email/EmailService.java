package com.sondos.qa_buddy.shared.email;

import com.sondos.qa_buddy.users.entity.User;

public interface EmailService {
	void sendRegistrationEmail(User user);
}
