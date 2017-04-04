package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This interface provides a set of default methods for accessing files and
 * directories during parsing of included Asciidoctor documents. They are based
 * on a {@link PreprocessorReader} and a {@link Document} instance.
 * <p>
 * Since the methods are already implemented, this Java interface is called
 * <i>Mixin</i>.
 */
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
	default File getRelative(Path dir, File file) {
		if (dir == null)
			return file;
		if (file == DIRECT_INPUT_FILE)
			return file;
		return dir.relativize(file.toPath()).toFile();
	}

	/**
	 * Returns a file that is relative to the base dir.
	 */
	default File getBaseRelative(File file) {
		return getRelative(getBasedir(), file);
	}

	/**
	 * Returns a file that is relative to the current dir.
	 */
	default File getCurrentDirRelative(File file) {
		return getRelative(getCurrentDir(), file);
	}

	/**
	 * Returns the current file relative to the base dir.
	 */
	default File getCurrentFileBaseRelative() {
		return getRelative(getBasedir(), getCurrentFile());
	}

	/**
	 * Returns an absolute file given a relative path. The given relative path
	 * is added to the base dir.
	 */
	default File getAbsoluteFileFromBaseDirectory(Path path) {
		Path basedir = getBasedir();
		if (basedir == null)
			return path.toFile();
		return basedir.resolve(path).normalize().toFile();
	}

	/**
	 * Returns an absolute file given a relative path. The given relative path
	 * is added to the base dir.
	 */
	default File getAbsoluteFileFromCurrentDirectory(Path path) {
		Path curdir = getCurrentDir();
		if (curdir == null)
			return path.toFile();
		return curdir.resolve(path).normalize().toFile();
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
	default Path getCurrentDir() {
		return Paths.get(getReader().getDir());
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
