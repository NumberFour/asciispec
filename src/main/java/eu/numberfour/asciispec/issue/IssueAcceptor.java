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

import java.io.File;

import org.asciidoctor.ast.ContentNode;

/**
 * Exposes an API to file issues encountered during document processing.
 */
public interface IssueAcceptor {
	/**
	 * Accepts the given issue.
	 *
	 * @param issue
	 *            the issue to accept
	 * @return the accepted issue
	 */
	public Issue accept(Issue issue);

	/**
	 * Creates and accepts a warning with the given parameters.
	 *
	 * @param parentNode
	 *            the parent node of the node being processed
	 * @param message
	 *            the message
	 * @return the newly created issue
	 */
	public default Issue warn(ContentNode parentNode, String message) {
		return accept(new Issue(parentNode, Issue.Severity.WARN, message));
	}

	/**
	 * Creates and accepts a warning with the given parameters.
	 *
	 * @param parentNode
	 *            the parent node of the node being processed
	 * @param message
	 *            the message
	 * @param file
	 *            the file containing the source of the issue
	 * @return the newly created issue
	 */
	public default Issue warn(ContentNode parentNode, String message, File file, int line) {
		return accept(new IssueInFile(parentNode, Issue.Severity.WARN, message, file, line));
	}

	/**
	 * Creates and accepts an error with the given parameters.
	 *
	 * @param parentNode
	 *            the parent node of the node being processed
	 * @param message
	 *            the message
	 * @return the newly created issue
	 */
	public default Issue error(ContentNode parentNode, String message) {
		return accept(new Issue(parentNode, Issue.Severity.ERROR, message));
	}

	/**
	 * Creates and accepts an error with the given parameters.
	 *
	 * @param parentNode
	 *            the parent node of the node being processed
	 * @param message
	 *            the message
	 * @param file
	 *            the file containing the source of the issue
	 * @return the newly created issue
	 */
	public default Issue error(ContentNode parentNode, String message, File file, int line) {
		return accept(new IssueInFile(parentNode, Issue.Severity.ERROR, message, file, line));
	}
}
