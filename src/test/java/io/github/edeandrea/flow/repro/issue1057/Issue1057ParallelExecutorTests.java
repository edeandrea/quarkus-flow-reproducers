package io.github.edeandrea.flow.repro.issue1057;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.edeandrea.flow.repro.support.Email;
import io.quarkus.test.junit.QuarkusTest;

/**
 * quarkiverse/quarkus-flow#1057. The app builds and boots (this test class runs at all), but the first invocation of a
 * workflow using a @ParallelAgent with a @ParallelExecutor fails with UnsupportedOperationException.
 */
@QuarkusTest
class Issue1057ParallelExecutorTests {
	@Inject
	ParallelExecutorFlow flow;

	@Test
	void aParallelAgentWithAParallelExecutorRuns() {
		assertThatCode(() -> this.flow.instance(new Email("My car was stolen.", false)).start().get(30, TimeUnit.SECONDS))
			.doesNotThrowAnyException();
	}
}
