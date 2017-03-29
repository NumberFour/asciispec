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

import eu.numberfour.asciispec.processors.ResolveIncludeProcessor;

/**
 * This exception is thrown when the {@link ResolveIncludeProcessor} notices that some includes are not used with the
 * find-macro. Only when all includes are used with the find-macro, cyclic dependencies can be detected. The error
 * message (see {@link #getMessage()}) indicates, which file was included without the find-macro.
 */
public class CannotFindCircularDependenciesException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final File notInStackFile;

	/**
	 * @param notInStackFile
	 *            which is not in the include stack
	 */
	public CannotFindCircularDependenciesException(File notInStackFile) {
		this.notInStackFile = notInStackFile;
	}

	/**
	 * States file which was included without the find-macro. (also refer to {@link Exception#getMessage()}
	 */
	@Override
	public String getMessage() {
		String str = "Cannot detect circular dependencies. ";
		str += "Probably 'include' was used without '{find}' macro when including file:\n\t";
		str += notInStackFile.toString();

		return str;
	}

	/**
	 * Returns the file which was not found in the stack and hence causes this exception to show up.
	 */
	public File getFileNotInStack() {
		return notInStackFile;
	}

}
