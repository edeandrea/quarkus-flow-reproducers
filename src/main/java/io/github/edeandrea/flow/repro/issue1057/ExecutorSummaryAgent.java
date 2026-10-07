package io.github.edeandrea.flow.repro.issue1057;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface ExecutorSummaryAgent {
	@Agent(value = "ExecutorSummaryAgent", outputKey = "summary")
	@UserMessage("ExecutorSummaryAgent: {email}")
	String summarize(@V("email") String email);
}
