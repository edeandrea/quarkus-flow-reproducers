package io.github.edeandrea.flow.repro.issue1057;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface ExecutorDetailsAgent {
	@Agent(value = "ExecutorDetailsAgent", outputKey = "details")
	@UserMessage("ExecutorDetailsAgent: {email}")
	String details(@V("email") String email);
}
