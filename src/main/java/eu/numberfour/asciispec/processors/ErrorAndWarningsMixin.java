package eu.numberfour.asciispec.processors;

import java.io.File;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.issue.IssueAcceptor;

/**
 * This interface provides a set of default methods for printing out errors and
 * warnings during parsing of included Asciidoctor documents. They are based on
 * an {@link IssueAcceptor}, the current file and line number.
 * <p>
 * Since the methods are already implemented, this Java interface is called
 * <i>Mixin</i>.
 */
public interface ErrorAndWarningsMixin {

	/**
	 * Returns the current file which is relative to the document's base
	 * directory.
	 */
	File getCurrentFileBaseRelative();

	/**
	 * Returns the current line.
	 */
	int getCurrentLine();

	/**
	 * Returns an {@link IssueAcceptor} for printing out issues.
	 */
	IssueAcceptor getIssueAcceptor();

	/**
	 * Works just like {@link #error(Document, String, String)} except that the
	 * console error message and returned inline error message have the same
	 * content.
	 *
	 * @param document
	 *            The document which is currently being processed.
	 * @param msg
	 *            The error message
	 * @return Formatted error message
	 */
	default String error(Document document, String msg) {
		return error(document, msg, msg);
	}

	/**
	 * Prints the passed error message in the console with some additional
	 * information. Formats and returns the inline error message.
	 *
	 * @param document
	 *            The document which is currently being processed.
	 * @param consoleMsg
	 *            The error message that should be printed in the console
	 * @param inlineMsg
	 *            The error message that should be shown to the user
	 * @return Formatted error message
	 */
	default String error(Document document, String consoleMsg, String inlineMsg) {
		getIssueAcceptor().error(document, consoleMsg, getCurrentFileBaseRelative(), getCurrentLine());
		return "#[Error: " + inlineMsg + "]#";
	}

	/**
	 * Works just like {@link #warn(Document, String, String)} except that the
	 * console warn message and returned warn message have the same content.
	 *
	 * @param document
	 *            The document which is currently being processed.
	 * @param msg
	 *            The warn message
	 * @return Formatted warn message
	 */
	default String warn(Document document, String msg) {
		return warn(document, msg, msg);
	}

	/**
	 * Prints the passed error message in the console with some additional
	 * information. Formats and returns the inline error message.
	 *
	 * @param document
	 *            The document which is currently being processed.
	 * @param consoleMsg
	 *            The warn message that should be printed in the console
	 * @param inlineMsg
	 *            The warn message that should be shown to the user
	 * @return Formatted warn message
	 */
	default String warn(Document document, String consoleMsg, String inlineMsg) {
		getIssueAcceptor().warn(document, consoleMsg, getCurrentFileBaseRelative(), getCurrentLine());
		return "#[Warn: " + inlineMsg + "]#";
	}
}
