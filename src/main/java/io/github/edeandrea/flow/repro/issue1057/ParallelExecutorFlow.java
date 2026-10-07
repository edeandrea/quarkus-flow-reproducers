package io.github.edeandrea.flow.repro.issue1057;

import static io.quarkiverse.flow.dsl.FlowDSL.function;
import static io.quarkiverse.flow.dsl.FlowDSL.tasks;
import static io.quarkiverse.flow.dsl.FlowWorkflowBuilder.workflow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.github.edeandrea.flow.repro.support.Email;
import io.github.edeandrea.flow.repro.support.Result;
import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;

@ApplicationScoped
public class ParallelExecutorFlow extends Flow {
	@Inject
	ParallelWithExecutorAgent agent;

	@Override
	public Workflow descriptor() {
		return workflow("parallel-executor-flow")
			.tasks(tasks(function("extract", (Email email) -> new Result(this.agent.extract(email.text())), Email.class, Result.class)))
			.build();
	}
}
