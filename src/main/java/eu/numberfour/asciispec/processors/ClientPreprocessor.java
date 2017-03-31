package eu.numberfour.asciispec.processors;

import java.util.List;

import org.asciidoctor.ast.Document;

public interface ClientPreprocessor {

	/**
	 * Is called before any line is processed.
	 */
	default void init(Document document) {
	}

	/**
	 * Enables the use of e.g. <code>{find}</code> variables within include
	 * directives like:<br/>
	 * <br/>
	 * <code>include::{find}myfile.adoc[]</code>
	 * <p>
	 * Otherwise, the line that contains this (unknown) variable is dropped by
	 * Asciidoctor!
	 *
	 * @return List of variable names
	 */
	default String getIncludeEnabledVariable() {
		return null;
	}

	/**
	 * Returns true iff {@link #processLine(Document, String)} shall be called.
	 */
	default boolean isEnabled() {
		return true;
	}

	/**
	 * Processes each line of the document. Comment regions are omitted. The
	 * passed line gets transformed multiple times, once per pattern registered
	 * in {@link #init(Document)}.
	 *
	 * @return a list of resulting lines
	 */
	List<String> processLine(Document document, String line);

}
