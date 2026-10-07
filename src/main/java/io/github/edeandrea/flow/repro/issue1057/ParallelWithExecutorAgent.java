package io.github.edeandrea.flow.repro.issue1057;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.agentic.declarative.ParallelExecutor;
import dev.langchain4j.service.V;
import io.opentelemetry.context.Context;

/** A plain @ParallelAgent with a standard LangChain4j @ParallelExecutor. Builds and boots; fails on first use. */
public interface ParallelWithExecutorAgent {
	@ParallelAgent(outputKey = "summary", subAgents = { ExecutorDetailsAgent.class, ExecutorSummaryAgent.class })
	String extract(@V("email") String email);

	@ParallelExecutor
	static Executor executor() {
		return Context.taskWrapping(Executors.newVirtualThreadPerTaskExecutor());
	}
}
