package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This preprocessor is aware of the file from which a current line (in
 * {@link #processLine(Document, String)}) was read from.
 * <p>
 * This class inherits from {@link Preprocessor} to keep the registration like
 * AsciiDoctorJ does. It is not integrated in the AsciiDoctor preprocessor chain
 * directly. Instead, it is indirectly executed from the
 * {@link HostPreprocessor} which is the only {@link Preprocessor} that is
 * integrated in the AsciiDoctor preprocessor chain.
 * <p>
 * The reason for the indirect execution is that only the first AsciiDoctor
 * {@link Preprocessor} has reliable information about line numbers, files and
 * paths.
 */
abstract public class FileAwarePreprocessor extends Preprocessor implements ClientPreprocessor {

	public FileAwarePreprocessor() {
		HostPreprocessor.getSingleton().register(this);
	}

	/**
	 * "Do nothing here!" is the intended use! The computation is done in
	 * {@link HostPreprocessor}!
	 */
	@Override
	final public void process(Document document, PreprocessorReader preproReader) {
		// (see JavaDoc above)
	}

	/**
	 * Searches for the given file in the directory of the current file.
	 */
	public File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException {
		return getHostPreprocessor().searchFile(fileName);
	}

	/**
	 * Returns a file that is relative to the base dir.
	 */
	public String getBaseRelative(File file) {
		return getHostPreprocessor().getBaseRelative(file);
	}

	/**
	 * Returns the file of the current adoc line.
	 */
	public File getCurrentFile() {
		return getHostPreprocessor().getCurrentFile();
	}

	/**
	 * Returns the path of the current adoc line.
	 */
	public File getCurrentDir() {
		return getHostPreprocessor().getCurrentDir();
	}

	/**
	 * Returns the line number of the current file.
	 */
	public int getCurrentLine() {
		return getHostPreprocessor().getCurrentLine();
	}

	final protected HostPreprocessor getHostPreprocessor() {
		return HostPreprocessor.getSingleton();
	}

}
