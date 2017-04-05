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

	/**
	 * Sets the {@link HostPreprocessor} during registration process.
	 */
	void setHostProcessor(HostPreprocessor hostPreprocessor);

	/**
	 * Returns the {@link HostPreprocessor}.
	 */
	HostPreprocessor getHostPreprocessor();

}
