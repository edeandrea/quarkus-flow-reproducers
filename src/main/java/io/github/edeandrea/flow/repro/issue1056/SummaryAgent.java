package io.github.edeandrea.flow.repro.issue1056;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface SummaryAgent {
	@Agent(value = "SummaryAgent", outputKey = "summary")
	@UserMessage("SummaryAgent: {email}")
	String summarize(@V("email") String email);
}
