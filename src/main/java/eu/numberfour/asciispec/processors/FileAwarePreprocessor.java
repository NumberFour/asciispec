package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

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
abstract public class FileAwarePreprocessor extends Preprocessor implements ClientPreprocessor, DirectoriesMixin {

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

	@Override
	public Path getBasedir() {
		return getHostPreprocessor().getBasedir();
	}

	@Override
	public Document getDocument() {
		return getHostPreprocessor().getDocument();
	}

	@Override
	public PreprocessorReader getReader() {
		return getHostPreprocessor().getReader();
	}

	final protected HostPreprocessor getHostPreprocessor() {
		return HostPreprocessor.getSingleton();
	}

	/*
	 * Redirect mixin methods.
	 */
	@Override
	public File getCurrentFileBaseRelative() {
		return DirectoriesMixin.super.getCurrentFileBaseRelative();
	}

	@Override
	public int getCurrentLine() {
		return DirectoriesMixin.super.getCurrentLine();
	}

	@Override
	public File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException {
		return DirectoriesMixin.super.searchFile(fileName);
	}

}
