package io.github.edeandrea.flow.repro.support;

/** Workflow input. {@code reenterTaskSpan} turns on the workaround described in issue 1056. */
public record Email(String text, boolean reenterTaskSpan) {
	public Email() {
		this(null, false);
	}
}
