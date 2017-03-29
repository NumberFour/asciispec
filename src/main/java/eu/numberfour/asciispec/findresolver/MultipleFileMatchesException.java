/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.asciispec.findresolver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * The find macro searches for files and might find more than one file only. In such cases, the user is informed using
 * this exception.
 */
public class MultipleFileMatchesException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** File name that returns multiple matches */
	public final String fileName;
	/** Contains all matched files */
	public final List<File> matches;
	/** Path of base directory */
	public final Path basedir;

	/**
	 * @param fileName
	 *            the file name that returns multiple matches
	 * @param matches
	 *            the files found
	 * @param basedir
	 *            the path of the base file
	 *
	 */
	public MultipleFileMatchesException(String fileName, List<File> matches, Path basedir) {
		this.fileName = fileName;
		this.matches = matches;
		this.basedir = basedir;
	}

	@Override
	public String getMessage() {
		String msg = "File '" + fileName + "' was found at multiple locations:";
		for (File f : matches) {
			String relFile = basedir.relativize(f.toPath()).toString();
			msg += "\n\t- " + relFile;
		}
		return msg;
	}

}
