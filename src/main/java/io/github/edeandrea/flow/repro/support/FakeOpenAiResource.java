package io.github.edeandrea.flow.repro.support;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * A stand-in for the OpenAI chat completions endpoint, served by the app itself, so the reproducers make real
 * LangChain4j AI-service calls (and so produce real {@code langchain4j.aiservices.*} spans) with no API key, no network
 * and no containers. See {@code quarkus.langchain4j.openai.base-url} in {@code application.properties}.
 */
@Path("/fake-openai/v1/chat/completions")
public class FakeOpenAiResource {
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String chatCompletion(String ignored) {
		return """
			{
				"id": "chatcmpl-fake",
				"object": "chat.completion",
				"created": 1234567890,
				"model": "fake",
				"choices": [ { "index": 0, "message": { "role": "assistant", "content": "ok" }, "finish_reason": "stop" } ],
				"usage": { "prompt_tokens": 1, "completion_tokens": 1, "total_tokens": 2 }
			}
			""";
	}
}
