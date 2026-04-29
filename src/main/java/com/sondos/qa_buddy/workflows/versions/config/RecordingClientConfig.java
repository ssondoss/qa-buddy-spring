package com.sondos.qa_buddy.workflows.versions.config;

import com.sondos.qa_buddy.workflows.versions.client.HttpRecordingNodeClient;
import com.sondos.qa_buddy.workflows.versions.client.RecordingNodeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecordingClientConfig {
	@Bean
	RecordingNodeClient recordingNodeClient(
			@Value("${app.recording.node.base-url:http://localhost:3000}") String baseUrl,
			@Value("${app.recording.node.start-path:/api/recordings/start}") String startPath,
			@Value("${app.recording.shared-secret:qa-buddy-recording-shared-secret}") String sharedSecret
	) {
		return new HttpRecordingNodeClient(baseUrl, startPath, sharedSecret);
	}
}
