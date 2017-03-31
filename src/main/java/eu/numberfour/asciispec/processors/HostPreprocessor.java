package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This preprocessor is aware of the file from which a current line (in
 * {@link #processLine(Document, String)}) was read from.
 */
abstract public class HostPreprocessor extends Preprocessor {

	private static HostPreprocessor singleton;

	public static HostPreprocessor getSingleton() {
		return singleton;
	}

	//// non-static members ////
	private PreprocessorReader reader;
	private Path basedir;
	private List<ClientPreprocessor> clientPreprocessors = new LinkedList<>();

	HostPreprocessor() {
		singleton = this;
	}

	public void register(ClientPreprocessor clientPreprocessor) {
		clientPreprocessors.add(clientPreprocessor);
	}

	private void setIncludedVariables(Document document) {
		for (ClientPreprocessor cp : clientPreprocessors) {
			String inclVar = cp.getIncludeEnabledVariable();
			document.setAttr(inclVar, "{" + inclVar + "}", true);
		}
	}

	@Override
	final public void process(Document document, PreprocessorReader preproReader) {
		reader = preproReader;
		basedir = AdocUtils.getDocumentBasePath(document);
		File documentBaseFile = AdocUtils.getDocumentBaseFile(document);
		if (documentBaseFile != null) {
			documentBaseFile = new File(getBaseRelative(documentBaseFile));
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

		List<String> processedLines = AdocUtils.processLine(newLine, (String ll, Integer lineNumber) -> {
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

	/**
	 * Searches for the given file in the directory of the current file.
	 */
	public File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException {
		return FileStackHelper.searchRelativeTo(fileName, getCurrentDir(), basedir);
	}

	/**
	 * Returns a file that is relative to the base dir.
	 */
	public String getBaseRelative(File file) {
		return basedir.relativize(file.toPath()).toString();
	}

	/**
	 * Returns the file of the current adoc line.
	 */
	public File getCurrentFile() {
		return new File(reader.getFile());
	}

	/**
	 * Returns the path of the current adoc line.
	 */
	public File getCurrentDir() {
		return new File(reader.getDir());
	}

	/**
	 * Returns the line number of the current file.
	 */
	public int getCurrentLine() {
		return reader.getLineNumber() - 1;
	}

}
