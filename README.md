# quarkus-flow reproducers

Runnable reproducers for three [quarkiverse/quarkus-flow](https://github.com/quarkiverse/quarkus-flow) bugs:

| Issue | Package | Test |
|---|---|---|
| [#1056](https://github.com/quarkiverse/quarkus-flow/issues/1056): task spans are never current in task bodies, and generated agentic sub-workflows always start a new trace | `issue1056` | `Issue1056TraceContextTests` |
| [#1057](https://github.com/quarkiverse/quarkus-flow/issues/1057): `@ParallelExecutor` on a `@ParallelAgent` throws `UnsupportedOperationException` at runtime | `issue1057` | `Issue1057ParallelExecutorTests` |
| [#1058](https://github.com/quarkiverse/quarkus-flow/issues/1058): `cancel()` on a waiting instance loses the workflow span | `issue1058` | `Issue1058CancelSpanTests` |

Each test asserts the **expected** behaviour, so **on an affected version the tests fail**. That's the reproduction. They
also print the exported spans grouped by trace, so you can see the result either way.

## Running

Needs Java 21+ only. No API keys, no network and no containers: the LangChain4j AI services call `FakeOpenAiResource`, a
stand-in OpenAI chat-completions endpoint served by the app itself, so they make real AI-service calls (and produce real
`langchain4j.aiservices.*` spans). Spans go to an in-memory `SpanExporter` bean.

```bash
./mvnw test                                    # quarkus-flow 1.1.3
./mvnw test -Dquarkus.flow.version=1.2.0.CR3   # another release
./mvnw test -Dtest=Issue1056TraceContextTests  # one issue
```

Versions: Quarkus 3.40.1, quarkus-langchain4j 1.14.1, quarkus-flow 1.1.3 by default (see `pom.xml`).

## What you should see on an affected version

**#1056**: `theTaskSpanIsCurrentInsideTheTaskBody` prints `Span.current() = 0000000000000000` while the task's own
`task.execute` span exists. One run of `TracingFlow` (one task calling `sequence(classify, parallel(details, summary))`)
produces 6 separate traces. With the task span re-entered by hand around the agent call, the first AI call joins the
workflow's trace, but the generated sub-workflows and the later AI calls are still separate: 5 traces.

**#1057**: the app builds and boots (the test runs at all), then the first invocation fails with
`UnsupportedOperationException: Changing the default WorkflowApplication executor is not supported at this time`.

**#1058**: 10 waiting instances are cancelled. Each one's `task.execute waitForDecision` span is exported, but its
`workflow.execute review-flow` span isn't, and `No instrumentation context was found` is logged for each.
