package io.github.edeandrea.flow.repro.issue1056;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;

/** sequence(classify, parallel(details, summary)): three AI-service calls, two generated sub-workflows. */
public interface IntakeAgents {
	@SequenceAgent(outputKey = "summary", subAgents = { ClassifierAgent.class, ExtractionAgent.class })
	String process(@V("email") String email);
}
