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
 *
 */
public class IssueInFile extends Issue {
	final File file;
	final int line;

	/**
	 * Creates a new issue with the given parameters.
	 *
	 * @param node
	 *            the node
	 * @param severity
	 *            the severity
	 * @param message
	 *            the message
	 * @param file
	 *            the file containing the source of the issue
	 * @param line
	 *            the line number
	 */
	public IssueInFile(ContentNode node, Severity severity, String message, File file, int line) {
		super(node, severity, message);
		this.file = file;
		this.line = line;
	}

	@Override
	public File getDocumentFile() {
		return file;
	}

	@Override
	public int getLineNumber() {
		return line;
	}

	@Override
	public boolean hasLineNumber() {
		return line != -1;
	}

}
