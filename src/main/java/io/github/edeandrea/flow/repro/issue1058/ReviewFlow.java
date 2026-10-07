package io.github.edeandrea.flow.repro.issue1058;

import static io.quarkiverse.flow.dsl.FlowDSL.listen;
import static io.quarkiverse.flow.dsl.FlowDSL.tasks;
import static io.quarkiverse.flow.dsl.FlowDSL.toOne;
import static io.quarkiverse.flow.dsl.FlowWorkflowBuilder.workflow;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;

/** Waits for an event that never comes, so the test can cancel it while it is WAITING. No agents involved. */
@ApplicationScoped
public class ReviewFlow extends Flow {
	public static final String NAME = "review-flow";

	@Override
	public Workflow descriptor() {
		return workflow(NAME)
			.tasks(tasks(listen("waitForDecision", toOne("io.github.edeandrea.review.decided"))))
			.build();
	}
}
