package io.github.edeandrea.flow.repro.issue1056;

import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.service.V;

public interface ExtractionAgent {
	@ParallelAgent(outputKey = "summary", subAgents = { DetailsAgent.class, SummaryAgent.class })
	String extract(@V("email") String email);
}
