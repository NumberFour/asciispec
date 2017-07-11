package eu.numberfour.asciispec.processors;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.SourceProcessor;

/**
 * This preprocessor is hosts all other Preprocessors. In fact, this is the only
 * {@link Preprocessor} which is truly registered at AsciiDoctorJ.
 * <p>
 * To be still able to chose a certain set of registered {@link Preprocessor}s,
 * all other Preprocessors still have to register like a true
 * {@link Preprocessor}, i.e. in the Manifest file or for test scenarios in the
 * test initializing method. For further details, also consult
 * {@link JavaExtensionRegistry}.
 * <p>
 * For implementing a new {@link Preprocessor}, it is recommended to extend
 * {@link FileAwarePreprocessor}. Alternatively, extend {@link Preprocessor} and
 * implement {@link ClientPreprocessor}.
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

	//// non-static members ////
	private Document document;
	private PreprocessorReader reader;
	private List<ClientPreprocessor> clientPreprocessors = new LinkedList<>();

	/**
	 * Registers a new {@link ClientPreprocessor}.
	 */
	public void register(ClientPreprocessor clientPreprocessor) {
		clientPreprocessors.add(clientPreprocessor);
		clientPreprocessor.setHostProcessor(this);
	}

	private void setIncludedVariables(Document document) {
		for (String inclVar : INCLUDE_VARIABLES) {
			document.setAttr(inclVar, "{" + inclVar + "}", true);
		}
	}

	@Override
	final public void process(Document document, PreprocessorReader reader) {
		this.document = document;
		this.reader = reader;
		File documentBaseFile = getBaseFile();
		if (documentBaseFile != null) {
			documentBaseFile = getBaseRelative(documentBaseFile);
		}

		setIncludedVariables(document);

		for (ClientPreprocessor cp : clientPreprocessors) {
			cp.init(document);
		}

		processLines(document);

		for (ClientPreprocessor cp : clientPreprocessors) {
			cp.finish(document);
		}
	}

	private void processLines(Document document) {
		LinkedList<String> newlines = new LinkedList<>();
		SourceProcessor sp = new SourceProcessor();

		while (reader.hasMoreLines()) {
			String line = reader.readLine();
			List<String> cplines = new LinkedList<String>();
			expand(document, sp, line, clientPreprocessors, cplines);
			newlines.addAll(cplines);
		}

		reader.restoreLines(newlines);
	}

	private void expand(Document document, SourceProcessor sp, String newLine, List<ClientPreprocessor> cps,
			List<String> result) {

		if (newLine == null)
			return;
		if (cps.isEmpty()) {
			result.add(newLine);
			sp.updateBlockState(newLine);
			return;
		}

		ClientPreprocessor cp = cps.get(0);
		List<ClientPreprocessor> tail = new LinkedList<>(cps);
		tail.remove(0);

		List<String> processedLines = sp.processWithoutUpdate(newLine, (String ll) -> {
			final List<String> replacement;
			if (cp.isEnabled()) {
				replacement = cp.processLine(document, ll);
			} else {
				replacement = Collections.emptyList();
				replacement.add(ll);
			}
			return replacement;
		});

		for (String processedLine : processedLines) {
			expand(document, sp, processedLine, tail, result);
		}
	}

	@Override
	public PreprocessorReader getReader() {
		return reader;
	}

	@Override
	public Document getDocument() {
		return document;
	}

}
