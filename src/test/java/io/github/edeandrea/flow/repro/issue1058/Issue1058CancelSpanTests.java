package io.github.edeandrea.flow.repro.issue1058;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.edeandrea.flow.repro.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.junit.QuarkusTest;
import io.serverlessworkflow.impl.WorkflowStatus;

/**
 * quarkiverse/quarkus-flow#1058. Cancel a WAITING instance several times; each cancelled instance should export its
 * workflow.execute span. It's a race, so the test counts how many are lost.
 */
@QuarkusTest
class Issue1058CancelSpanTests {
	private static final int RUNS = 10;

	@Inject
	ReviewFlow flow;

	@Inject
	InMemorySpanExporter exporter;

	@BeforeEach
	void reset() {
		this.exporter.reset();
	}

	@Test
	void cancellingAWaitingInstanceExportsItsWorkflowSpan() throws Exception {
		var instanceIds = new ArrayList<String>();

		for (var i = 0; i < RUNS; i++) {
			var instance = this.flow.instance();
			instance.start();

			Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> instance.status() == WorkflowStatus.WAITING);
			assertThat(instance.cancel()).isTrue();
			instanceIds.add(instance.id());
		}

		// The task spans of every cancelled instance arrive; give the workflow spans the same chance
		Awaitility.await()
		          .atMost(Duration.ofSeconds(10))
		          .until(() -> this.exporter.spans().stream().filter(span -> span.getName().equals("task.execute waitForDecision")).count() >= RUNS);
		Thread.sleep(1000);

		var lost = instanceIds.stream().filter(id -> workflowSpan(id) == null).toList();

		System.out.printf("%n===== %d of %d cancelled instances have no exported workflow.execute span =====%n", lost.size(), RUNS);
		this.exporter.printTraces("after cancelling " + RUNS + " waiting instances",
			span -> ReviewFlow.NAME.equals(String.valueOf(InMemorySpanExporter.attributes(span).get("flow.workflow.name"))));

		assertThat(lost)
			.as("cancelled instances whose workflow.execute span was never exported")
			.isEmpty();
	}

	private SpanData workflowSpan(String instanceId) {
		return this.exporter.spans()
		                    .stream()
		                    .filter(span -> span.getName().equals("workflow.execute " + ReviewFlow.NAME))
		                    .filter(span -> instanceId.equals(String.valueOf(InMemorySpanExporter.attributes(span).get("flow.workflow.instance.id"))))
		                    .findFirst()
		                    .orElse(null);
	}
}
