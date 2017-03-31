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
import java.util.Objects;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.StructuralNode;

import eu.numberfour.asciispec.AdocUtils;

/**
 * Instances of this class represent issues encountered during processing or validation of a document.
 */
public class Issue {
	/**
	 * The severity of an issue.
	 */
	public static enum Severity {
		/**
		 * Warning
		 */
		WARN,
		/**
		 * Error
		 */
		ERROR
	}

	private final ContentNode node;
	private final Severity severity;
	private final String message;

	/**
	 * Creates a new issue with the given parameters.
	 *
	 * @param node
	 *            the node
	 * @param severity
	 *            the severity
	 * @param message
	 *            the message
	 */
	public Issue(ContentNode node, Severity severity, String message) {
		this.node = Objects.requireNonNull(node);
		this.severity = Objects.requireNonNull(severity);
		this.message = Objects.requireNonNull(message);
	}

	/**
	 * Returns the value of the <code>:docfile:</code> attribute of the document that was being processed.
	 *
	 * @return a file representing the path to the file being processed
	 */
	public File getDocumentFile() {
		File docfile = AdocUtils.getDocumentBaseFile(node.getDocument());
		return docfile;
	}

	/**
	 * Indicates whether a document file is available for this issue.
	 *
	 * @return <code>true</code> if a document file is available fro this issue or <code>false</code> otherwise
	 */
	public boolean hasDocumentFile() {
		return getDocumentFile() != null;
	}

	/**
	 * Returns the line number at which this issue occurred. For this to work, the <code>sourcemap</code> option must be
	 * set to <code>true</code> when the document is processed.
	 *
	 * @return the line number or -1 if no line number information was available at the node
	 */
	public int getLineNumber() {
		if (!hasLineNumber())
			return -1;
		return ((StructuralNode) node).getSourceLocation().getLineNumber();
	}

	/**
	 * Indicates whether line number information is available for this issue.
	 *
	 * @return <code>true</code> if line number information is available for this issue or <code>false</code> otherwise
	 */
	public boolean hasLineNumber() {
		return node instanceof StructuralNode && ((StructuralNode) node).getSourceLocation() != null;
	}

	/**
	 * Returns the severity of this issue.
	 *
	 * @return the severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * Returns the message attached to this issue.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getSeverity());
		if (hasDocumentFile())
			result.append(": ").append(getDocumentFile().getName());
		if (hasLineNumber())
			result.append(": line ").append(getLineNumber());
		result.append(": ").append(getMessage());
		return result.toString();
	}
}
