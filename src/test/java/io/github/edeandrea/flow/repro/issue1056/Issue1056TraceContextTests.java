package io.github.edeandrea.flow.repro.issue1056;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.edeandrea.flow.repro.InMemorySpanExporter;
import io.github.edeandrea.flow.repro.support.Email;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.junit.QuarkusTest;

/**
 * quarkiverse/quarkus-flow#1056. Each test asserts the EXPECTED behaviour, so on an affected version it fails.
 * The span tree is printed to stdout either way.
 */
@QuarkusTest
class Issue1056TraceContextTests {
	private static final String AI_SERVICE_PREFIX = "langchain4j.aiservices.";

	@Inject
	TracingFlow flow;

	@Inject
	InMemorySpanExporter exporter;

	@BeforeEach
	void reset() {
		this.exporter.reset();
		TracingFlow.lastObservation = null;
	}

	/** Cause 1: the task's own span exists while the body runs, but it is not the current span. */
	@Test
	void theTaskSpanIsCurrentInsideTheTaskBody() throws Exception {
		run(false);

		System.out.printf("%nInside the task body: Span.current() = %s, the task.execute span = %s%n",
			TracingFlow.lastObservation.currentSpanId(), TracingFlow.lastObservation.taskSpanId());

		assertThat(TracingFlow.lastObservation.currentSpanId())
			.as("Span.current() inside the task body should be the task.execute span")
			.isEqualTo(TracingFlow.lastObservation.taskSpanId());
	}

	/** Cause 1, its effect: every LangChain4j AI-service span the task causes starts its own trace. */
	@Test
	void aiServiceSpansAreInTheWorkflowTrace() throws Exception {
		run(false);
		this.exporter.printTraces("plain run");

		var workflowTrace = span("workflow.execute " + TracingFlow.NAME).getTraceId();

		assertThat(this.exporter.spans())
			.filteredOn(span -> span.getName().startsWith(AI_SERVICE_PREFIX))
			.hasSize(3)
			.allSatisfy(span -> assertThat(span.getTraceId()).as(span.getName()).isEqualTo(workflowTrace));
	}

	/**
	 * Cause 2: even with the task span made current by hand around the agent call, the generated agentic sub-workflows
	 * (and the AI calls they drive) start new traces.
	 */
	@Test
	void generatedAgenticSubWorkflowsAreInTheCallingTrace() throws Exception {
		run(true);
		this.exporter.printTraces("task span re-entered around the agent call (workaround for cause 1)");

		var workflowTrace = span("workflow.execute " + TracingFlow.NAME).getTraceId();

		assertThat(this.exporter.spans())
			.filteredOn(span -> span.getName().startsWith("workflow.execute ") || span.getName().startsWith(AI_SERVICE_PREFIX))
			.hasSize(6)
			.allSatisfy(span -> assertThat(span.getTraceId()).as(span.getName()).isEqualTo(workflowTrace));
	}

	private void run(boolean reenterTaskSpan) throws Exception {
		this.flow.instance(new Email("My car was stolen.", reenterTaskSpan)).start().get(30, TimeUnit.SECONDS);

		// main workflow + 2 generated sub-workflows + 3 AI-service spans
		Awaitility.await()
		          .atMost(Duration.ofSeconds(10))
		          .until(() -> this.exporter.spans().stream().filter(span -> span.getName().startsWith("workflow.execute ") || span.getName().startsWith(AI_SERVICE_PREFIX)).count() >= 6);
	}

	private SpanData span(String name) {
		return this.exporter.spans().stream().filter(span -> name.equals(span.getName())).findFirst().orElseThrow(() -> new AssertionError("No span " + name));
	}
}
