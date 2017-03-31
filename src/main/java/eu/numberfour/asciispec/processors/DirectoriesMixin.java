package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

public interface DirectoriesMixin {

	Path getBasedir();

	PreprocessorReader getReader();

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
		return getBasedir().relativize(file.toPath()).toFile();
	}

	/**
	 * Returns the current file relative to the base dir.
	 */
	default File getCurrentFileBaseRelative() {
		File currentFile = getCurrentFile();
		if ("<DIRECT_INPUT>".equals(currentFile.getName())) {
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
		return getBasedir().resolve(path).toFile();
	}

	/**
	 * Returns the file of the current adoc line.
	 */
	default File getCurrentFile() {
		return new File(getReader().getFile());
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
}
