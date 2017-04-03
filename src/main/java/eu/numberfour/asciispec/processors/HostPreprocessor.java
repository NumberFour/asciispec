package eu.numberfour.asciispec.processors;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.AdocUtils;

/**
 * This preprocessor is aware of the file from which a current line (in
 * {@link #processLine(Document, String)}) was read from.
 */
public class HostPreprocessor extends Preprocessor implements DirectoriesMixin {

	//// public static members ////
	private static final List<String> INCLUDE_VARIABLES = new LinkedList<>();

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
	public static void enableIncludeVariable(String varName) {
		INCLUDE_VARIABLES.add(varName);
	}

	private static HostPreprocessor singleton;

	public static HostPreprocessor getSingleton() {
		return singleton;
	}

	//// non-static members ////
	private PreprocessorReader reader;
	private Path basedir;
	private List<ClientPreprocessor> clientPreprocessors = new LinkedList<>();

	public HostPreprocessor() {
		if (singleton != null) {
			singleton.clientPreprocessors.clear();
			reader = null;
		}
		singleton = this;
	}

	public void register(ClientPreprocessor clientPreprocessor) {
		clientPreprocessors.add(clientPreprocessor);
	}

	private void setIncludedVariables(Document document) {
		for (String inclVar : INCLUDE_VARIABLES) {
			document.setAttr(inclVar, "{" + inclVar + "}", true);
		}
	}

	@Override
	final public void process(Document document, PreprocessorReader preproReader) {
		reader = preproReader;
		basedir = AdocUtils.getDocumentBasePath(document);
		File documentBaseFile = AdocUtils.getDocumentBaseFile(document);
		if (documentBaseFile != null) {
			documentBaseFile = getBaseRelative(documentBaseFile);
		}

		setIncludedVariables(document);

		for (ClientPreprocessor cp : clientPreprocessors) {
			cp.init(document);
		}

		processLines(document);
	}

	private void processLines(Document document) {
		LinkedList<String> newlines = new LinkedList<>();

		while (reader.hasMoreLines()) {
			String line = reader.readLine();
			int ln = reader.getLineNumber();
			List<String> cplines = new LinkedList<String>();
			conc(document, line, clientPreprocessors, cplines);
			newlines.addAll(cplines);
		}

		reader.restoreLines(newlines);
	}

	private void conc(Document document, String newLine, List<ClientPreprocessor> cps, List<String> result) {
		if (newLine == null)
			return;
		if (cps.isEmpty()) {
			result.add(newLine);
			return;
		}

		ClientPreprocessor cp = cps.get(0);
		List<ClientPreprocessor> tail = new LinkedList<>(cps);
		tail.remove(0);

		List<String> processedLines = AdocUtils.processLine(newLine, (String ll) -> {
			final List<String> replacement;
			if (cp.isEnabled()) {
				replacement = cp.processLine(document, ll);
			} else {
				replacement = Collections.emptyList();
			}
			return replacement;
		});

		for (String processedLine : processedLines) {
			conc(document, processedLine, tail, result);
		}
	}

	@Override
	public Path getBasedir() {
		return basedir;
	}

	@Override
	public PreprocessorReader getReader() {
		return reader;
	}

}
