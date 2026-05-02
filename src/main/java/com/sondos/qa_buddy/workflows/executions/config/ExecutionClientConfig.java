package com.sondos.qa_buddy.workflows.executions.config;

import com.sondos.qa_buddy.workflows.executions.client.ExecutionNodeClient;
import com.sondos.qa_buddy.workflows.executions.client.HttpExecutionNodeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutionClientConfig {
	@Bean
	ExecutionNodeClient executionNodeClient(
			@Value("${app.execution.node.base-url:http://localhost:3000}") String baseUrl,
			@Value("${app.execution.node.start-path:/api/executions/start}") String startPath,
			@Value("${app.execution.shared-secret:qa-buddy-execution-shared-secret}") String sharedSecret
	) {
		return new HttpExecutionNodeClient(baseUrl, startPath, sharedSecret);
	}
}
