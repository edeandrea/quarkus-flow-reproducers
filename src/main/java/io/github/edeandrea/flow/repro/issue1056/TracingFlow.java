package io.github.edeandrea.flow.repro.issue1056;

import static io.quarkiverse.flow.dsl.FlowDSL.tasks;
import static io.quarkiverse.flow.dsl.FlowDSL.withFilter;
import static io.quarkiverse.flow.dsl.FlowWorkflowBuilder.workflow;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.github.edeandrea.flow.repro.support.Email;
import io.github.edeandrea.flow.repro.support.Result;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.quarkiverse.flow.Flow;
import io.quarkiverse.flow.opentelemetry.runtime.InstrumentationContext;
import io.quarkiverse.flow.opentelemetry.runtime.WorkflowInstrumentationContext;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.impl.TaskContextData;
import io.serverlessworkflow.impl.WorkflowContextData;

/** One task that calls an agentic topology, recording which span is current inside the task body. */
@ApplicationScoped
public class TracingFlow extends Flow {
	public static final String NAME = "tracing-flow";

	/** What the task body saw: the span current inside it, and the task's own {@code task.execute} span. */
	public record Observation(String currentSpanId, String taskSpanId) {
	}

	public static volatile Observation lastObservation;

	@Inject
	IntakeAgents agents;

	@Override
	public Workflow descriptor() {
		return workflow(NAME)
			.tasks(tasks(withFilter("runAgents", this::runAgents, Email.class, Result.class)))
			.build();
	}

	private Result runAgents(Email email, WorkflowContextData workflowContext, TaskContextData taskContext) {
		var taskSpan = taskSpan(workflowContext, taskContext);

		lastObservation = new Observation(
			Span.current().getSpanContext().getSpanId(),
			taskSpan.map(span -> span.getSpanContext().getSpanId()).orElse("none"));

		if (email.reenterTaskSpan() && taskSpan.isPresent()) {
			// The workaround: make Flow's own task span current around the call
			try (var scope = taskSpan.get().storeInContext(Context.current()).makeCurrent()) {
				return new Result(this.agents.process(email.text()));
			}
		}

		return new Result(this.agents.process(email.text()));
	}

	/** quarkus-flow-opentelemetry keeps each task's span in the instance metadata, behind public accessors. */
	private static Optional<Span> taskSpan(WorkflowContextData workflowContext, TaskContextData taskContext) {
		return Optional.ofNullable(WorkflowInstrumentationContext.getWorkflowInstrumentationContext(workflowContext.instanceData()))
		               .map(ctx -> ctx.getTaskInstanceContext(taskContext.position().jsonPointer(), taskContext.iteration(), taskContext.retryAttempt()))
		               .map(InstrumentationContext::getStartSpan);
	}
}
