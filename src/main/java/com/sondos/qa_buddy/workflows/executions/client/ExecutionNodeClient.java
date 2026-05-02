package com.sondos.qa_buddy.workflows.executions.client;

import com.sondos.qa_buddy.workflows.executions.dto.ExecutionRunNodeResponse;
import com.sondos.qa_buddy.workflows.executions.dto.ExecutionStartNodeRequest;

public interface ExecutionNodeClient {
	ExecutionRunNodeResponse executeVersion(ExecutionStartNodeRequest request);
}
