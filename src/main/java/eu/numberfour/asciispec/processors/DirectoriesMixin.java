package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Objects;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

public interface DirectoriesMixin {

	/**
	 * File name in test scenarios.
	 */
	String DIRECT_INPUT_FILE_NAME = "<DIRECT_INPUT>";

	/**
	 * Base file in test scenarios.
	 */
	File DIRECT_INPUT_FILE = new File(DIRECT_INPUT_FILE_NAME);

	PreprocessorReader getReader();

	Document getDocument();

	/**
	 * Searches for the given file in the directory of the current file.
	 */
	default File searchFile(String fileName) throws FileNotFoundException, MultipleFileMatchesException {
		return FileStackHelper.searchRelativeTo(fileName, getCurrentDir(), getBasedir());
	}

	/**
	 * Returns a file that is relative to the base dir.
	 */
	default File getBaseRelative(File file) {
		Path basedir = getBasedir();
		if (basedir == null)
			return file;
		if (file == DIRECT_INPUT_FILE)
			return file;
		return basedir.relativize(file.toPath()).toFile();
	}

	/**
	 * Returns the current file relative to the base dir.
	 */
	default File getCurrentFileBaseRelative() {
		File currentFile = getCurrentFile();
		if (currentFile == DIRECT_INPUT_FILE) {
			// happens in test scenarios
			return currentFile;
		}
		return getBaseRelative(currentFile);
	}

	/**
	 * Returns an absolute file given a relative path. The given relative path
	 * is added to the base dir.
	 */
	default File getAbsoluteFileFromBase(Path path) {
		Path basedir = getBasedir();
		if (basedir == null)
			return path.toFile();
		return basedir.resolve(path).toFile();
	}

	/**
	 * Returns the file of the current adoc line.
	 */
	default File getCurrentFile() {
		String fileName = getReader().getFile();
		if (fileName.equals(DIRECT_INPUT_FILE_NAME))
			return DIRECT_INPUT_FILE;
		return new File(fileName);
	}

	/**
	 * Returns the path of the current adoc line.
	 */
	default File getCurrentDir() {
		return new File(getReader().getDir());
	}

	/**
	 * Returns the line number of the current file.
	 */
	default int getCurrentLine() {
		return getReader().getLineNumber() - 1;
	}

	/** Returns the base dir. Can be null in test scenarios. */
	default Path getBasedir() {
		return AdocUtils.getDocumentBasePath(getDocument());
	}

	/**
	 * Returns the base file of the document.
	 *
	 * @param document
	 *            the document
	 * @return the path to the folder that contains the given document or
	 *         <code>null</code> if that path could not be determined
	 */
	default File getBaseFile() {
		Document document = getDocument();
		String baseFileName = AdocUtils.getAttributeAsString(Objects.requireNonNull(document), "docfile", null);
		if (baseFileName == null)
			return null;

		// The '<DIRECT_INPUT>' is set for tests only.
		// See: {@link AsciidoctorTest#getOptions(File, File)}
		if (baseFileName.equals(DIRECT_INPUT_FILE_NAME))
			return DIRECT_INPUT_FILE;

		return new File(baseFileName);
	}

}
