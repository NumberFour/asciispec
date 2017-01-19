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
package eu.numberfour.asciispec.math;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssueCollector;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnugglePackage;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.SnuggleSnapshot;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;

/**
 * Utility functions to convert LaTeX math expressions to MathML.
 */
public class MathService {

	private static final Map<Document, MathService> SERVICES = new WeakHashMap<>();

	/**
	 * Returns a math service for the given document.
	 *
	 * @param document
	 *            the document
	 * @return a math service
	 */
	public static MathService get(Document document) {
		Objects.requireNonNull(document);

		MathService service = SERVICES.get(document);
		if (service == null) {
			service = new MathService(document);
			SERVICES.put(document, service);
		}
		return service;
	}

	private final Document document;

	private final SnuggleEngine engine;

	private SnuggleSnapshot snapshot;

	private MathService(Document document) {
		this.document = document;
		this.engine = new SnuggleEngine();

		SessionConfiguration sessionConfig = new SessionConfiguration();
		sessionConfig.setFailingFast(true);
		this.snapshot = engine.createSession(sessionConfig).createSnapshot();
	}

	/**
	 * Includes the contents of the file at the given path when processing math.
	 *
	 * @param path
	 *            the path to a file containing LaTeX commands
	 */
	public void include(Path path) {
		Objects.requireNonNull(path);
		SnuggleInput input = new SnuggleInput(path.toFile());
		SnuggleSession session = this.snapshot.createSession();

		try {
			if (session.parseInput(input))
				this.snapshot = session.createSnapshot();
			else
				throw new IllegalArgumentException("File commands could not be parsed.");
		} catch (IOException e) {
			throw new IllegalArgumentException("File could not be loaded: " + e.getMessage());
		}
	}

	/**
	 * Converts an inline LaTeX math expression (without delimiter signs) to Math ML.
	 *
	 * @param expression
	 *            the expression to convert
	 * @return the converted MathML string, escaped with inline pass
	 */
	public String convertInline(String expression) {
		if (expression.trim().isEmpty())
			return "";

		if (expression.contains("$")) {
			String message = "Unable to parse inline math expression '" + escapeMathDelimiters(expression)
					+ "': Must not contain math delimiter '$'";
			throw new IllegalArgumentException(message);
		}

		SnuggleSession session = snapshot.createSession();
		SnuggleInput math = new SnuggleInput("$" + expression + "$");

		try {
			if (!session.parseInput(math)) {
				throw new IllegalArgumentException(
						"Unable to parse math expression '" + escapeMathDelimiters(expression) + "' ["
								+ getMathErrors(session) + "]");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Unexpected error while parsing math expression '" + escapeMathDelimiters(expression)
							+ "': " + e.getMessage());
		}

		return "+++" + session.buildXMLString() + "+++";
	}

	private String escapeMathDelimiters(String input) {
		return input.replaceAll("([^\\\\])\\$", "$1\\$");
	}

	/**
	 * Converts LaTeX block math expression (without delimiter signs) to Math ML.
	 *
	 * @param expression
	 *            the expression to convert, split into multiple lines
	 * @param issueAcceptor
	 *            the issue acceptor
	 * @return the converted MathML string, split into multiple lines, and enclosed in a pass block
	 */
	public List<String> convertBlock(List<String> expression, IssueAcceptor issueAcceptor) {
		IssueCollector issueCollector = new IssueCollector(issueAcceptor);

		ArrayList<String> result = new ArrayList<>();

		String joined = String.join("\n", expression);
		if (!joined.isEmpty()) {
			SnuggleSession session = snapshot.createSession();
			SnuggleInput math = new SnuggleInput("\\[" + joined + "\\]");

			try {
				if (!session.parseInput(math)) {
					blockError(issueCollector, session);
					result.add("```");
					result.addAll(expression);
					result.add("```");
				} else {
					String joinedResult = session.buildXMLString();
					result.add("++++");
					result.addAll(Arrays.asList(joinedResult));
					result.add("++++");
				}
			} catch (Throwable t) {
				blockError(issueCollector, "Unable to process math block: " + t.getMessage());
			}
		}

		issueCollector.appendTo(result);
		return result;
	}

	private void blockError(IssueAcceptor issueAcceptor, String message) {
		issueAcceptor.error(document, message);
	}

	private void blockError(IssueAcceptor issueAcceptor, SnuggleSession session) {
		issueAcceptor.error(document, "Math syntax error [" + getMathErrors(session) + "]");
	}

	private String getMathErrors(SnuggleSession session) {
		return session.getErrors().stream().map(error -> formatError(error))
				.collect(Collectors.joining(", "));
	}

	private String formatError(InputError error) {
		SnugglePackage defaultPackage = CorePackageDefinitions.getPackage();
		ResourceBundle errorMessageBundle = defaultPackage.getErrorMessageBundle();

		StringBuilder builder = new StringBuilder();
		builder.append("At '").append(error.getSlice().extract()).append("'");
		builder.append(": ").append(error.getErrorCode());
		builder.append(" - ").append(errorMessageBundle.getString(error.getErrorCode().getName()));

		return builder.toString();
	}
}
