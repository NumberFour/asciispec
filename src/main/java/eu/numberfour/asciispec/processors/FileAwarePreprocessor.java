package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This preprocessor is aware of the file from which a current line (in {@link #processLine(Document, String)}) was read
 * from.
 */
abstract public class FileAwarePreprocessor extends Preprocessor implements Supplier<String> {

	//// public static members ////
	private static final List<String> INCLUDE_VARIABLES = new LinkedList<>();

	/**
	 * Enables the use of e.g. <code>{find}</code> variables within include
	 * directives like:<br/>
	 * <br/>
	 * <code>include::{find}myfile.adoc[]</code>
	 */
	public static void enableIncludeVariable(String varName) {
		INCLUDE_VARIABLES.add(varName);
	}

	//// non-static members ////
	private PreprocessorReader reader;
	private Path basedir;

	/**
	 * Processes each line of the document. Comment regions are omitted. The passed line gets transformed multiple
	 * times, once per pattern registered in {@link #init(Document)}.
	 *
	 * @return a list of resulting lines
	 */
	abstract protected List<String> processLine(Document document, String line);

	/**
	 * Is called before any line is processed. The return value enables/disables the processor.
	 */
	abstract protected boolean init(Document document);

	@Override
	final public void process(Document document, PreprocessorReader preproReader) {
		reader = preproReader;
		basedir = AdocUtils.getDocumentBasePath(document);

		boolean enabled = init(document);
		if (enabled) {
			enableADocVariablesInIncludeProcessor(document);
			List<String> lines = processLines(document);
			reader.restoreLines(lines);
		}
	}

	private void enableADocVariablesInIncludeProcessor(Document document) {
		for (String inclVar : INCLUDE_VARIABLES) {
			document.setAttr(inclVar, "{" + inclVar + "}", true);
		}
	}

	@Override
	final public String get() {
		if (!reader.hasMoreLines())
			return null;
		return reader.readLine();
	}

	private List<String> processLines(Document document) {
		return AdocUtils.processLines(this, (String l) -> {
			final List<String> replacement = processLine(document, l);
			return replacement;
		});
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
		Path relPath = basedir.relativize(file.toPath());
		return relPath.toString();
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
