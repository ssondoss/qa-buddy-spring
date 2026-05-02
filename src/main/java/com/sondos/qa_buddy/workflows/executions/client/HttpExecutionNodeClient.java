package com.sondos.qa_buddy.workflows.executions.client;

import com.sondos.qa_buddy.workflows.executions.dto.ExecutionRunNodeResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStartNodeRequest;
import com.sondos.qa_buddy.workflows.executions.exception.ExecutionNodeUnavailableException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpExecutionNodeClient implements ExecutionNodeClient {
	private final RestClient restClient;
	private final String startPath;
	private final String sharedSecret;

	public HttpExecutionNodeClient(String baseUrl, String startPath, String sharedSecret) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
		this.startPath = startPath;
		this.sharedSecret = sharedSecret;
	}

	@Override
	public ExecutionRunNodeResponse executeVersion(ExecutionStartNodeRequest request) {
		try {
			ExecutionRunNodeResponse response = restClient.post()
					.uri(startPath)
					.header("X-Integration-Secret", sharedSecret)
					.body(request)
					.retrieve()
					.body(ExecutionRunNodeResponse.class);

			if (response == null) {
				throw new ExecutionNodeUnavailableException("Node execution service returned an empty response");
			}

			return response;
		} catch (RestClientException ex) {
			throw new ExecutionNodeUnavailableException("Unable to execute version in node service", ex);
		}
	}
}
