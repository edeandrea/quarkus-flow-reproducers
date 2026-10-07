package io.github.edeandrea.flow.repro;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/** Quarkus picks up a SpanExporter CDI bean automatically. */
@ApplicationScoped
public class InMemorySpanExporter implements SpanExporter {
	private final List<SpanData> spans = new CopyOnWriteArrayList<>();

	@Override
	public CompletableResultCode export(Collection<SpanData> spans) {
		this.spans.addAll(spans);
		return CompletableResultCode.ofSuccess();
	}

	@Override
	public CompletableResultCode flush() {
		return CompletableResultCode.ofSuccess();
	}

	@Override
	public CompletableResultCode shutdown() {
		return CompletableResultCode.ofSuccess();
	}

	public List<SpanData> spans() {
		return List.copyOf(this.spans);
	}

	public void reset() {
		this.spans.clear();
	}

	/** Prints the exported spans grouped by trace, so the number of separate traces is obvious in the test output. */
	public void printTraces(String title) {
		printTraces(title, span -> true);
	}

	public void printTraces(String title, java.util.function.Predicate<SpanData> filter) {
		var byTrace = spans().stream().filter(filter).collect(Collectors.groupingBy(SpanData::getTraceId, java.util.LinkedHashMap::new, Collectors.toList()));
		var names = spans().stream().collect(Collectors.toMap(SpanData::getSpanId, SpanData::getName, (a, b) -> a));

		System.out.printf("%n===== %s: %d spans in %d separate traces =====%n", title, byTrace.values().stream().mapToInt(List::size).sum(), byTrace.size());
		byTrace.forEach((traceId, spans) -> {
			System.out.printf("trace %s%n", traceId);
			spans.forEach(span -> System.out.printf("    %-60s parent=%s%n", span.getName(),
				span.getParentSpanContext().isValid() ? names.getOrDefault(span.getParentSpanId(), span.getParentSpanId()) : "<none: trace root>"));
		});
	}

	public static Map<String, Object> attributes(SpanData span) {
		return span.getAttributes().asMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getKey(), Map.Entry::getValue));
	}
}
