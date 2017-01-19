/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.asciispec.issue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * An issue acceptor that delegates to another issue acceptor, but collects all accepted issues.
 */
public class IssueCollector implements IssueAcceptor {

	private final List<Issue> issues = new LinkedList<>();

	private final IssueAcceptor delegate;

	/**
	 * Creates a new instance that delegates to the given acceptor.
	 *
	 * @param delegate
	 *            the acceptor to delegate to
	 */
	public IssueCollector(IssueAcceptor delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	/**
	 * Returns an unmodifiable list containing all issues collected by this collector.
	 *
	 * @return the collected issues
	 */
	public List<Issue> getIssues() {
		return Collections.unmodifiableList(issues);
	}

	/**
	 * Appends the result of calling {@link #getAsLines()} to the given list of lines.
	 *
	 * @param lines
	 *            the list to append to
	 */
	public void appendTo(List<String> lines) {
		getAsLines(lines);
	}

	/**
	 * Returns a list of strings representing lines that describe each collected issue. These lines can be appended to
	 * an Asciidoc block to render them.
	 *
	 * @return a list of lines describing the issues or an empty list if there are no issues
	 */
	public List<String> getAsLines() {
		if (issues.isEmpty())
			return Collections.emptyList();

		List<String> result = new ArrayList<>(issues.size() + 3);
		getAsLines(result);
		return result;
	}

	private void getAsLines(List<String> lines) {
		if (!issues.isEmpty()) {
			lines.add("");
			lines.add("[role=\"issues\"]");
			lines.add("--");
			lines.add("**Issues:**");
			lines.add("");
			for (Issue issue : issues)
				lines.add("- " + issue.getMessage());
			lines.add("");
			lines.add("--");
		}
	}

	@Override
	public Issue accept(Issue issue) {
		issues.add(issue);
		return delegate.accept(issue);
	}

}
