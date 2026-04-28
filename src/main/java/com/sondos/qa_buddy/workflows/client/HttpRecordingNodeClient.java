package com.sondos.qa_buddy.workflows.client;

import com.sondos.qa_buddy.workflows.exception.RecordingNodeUnavailableException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpRecordingNodeClient implements RecordingNodeClient {
	private final RestClient restClient;
	private final String startPath;
	private final String sharedSecret;

	public HttpRecordingNodeClient(String baseUrl, String startPath, String sharedSecret) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
		this.startPath = startPath;
		this.sharedSecret = sharedSecret;
	}

	@Override
	public void startRecording(RecordingStartRequest request) {
		try {
			restClient.post()
					.uri(startPath)
					.header("X-Integration-Secret", sharedSecret)
					.body(request)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException ex) {
			throw new RecordingNodeUnavailableException("Unable to start recording in node service", ex);
		}
	}
}
