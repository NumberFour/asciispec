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
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Circular dependencies can only be detected, when all includes use the find macro (see
 * {@link CannotFindCircularDependenciesException}). They exist when files have (indirect) self references. The error
 * message (see {@link #getMessage()}) indicates the dependency cycle listing all files on stack.
 */
public class CircularDependencyException extends Exception {
	private final List<String> locations = new LinkedList<>();

	/**
	 * @param locationStack
	 *            contains the dependency cycle
	 */
	public CircularDependencyException(Stack<File> locationStack, Path basedir) {
		for (File f : locationStack) {
			String relFile = basedir.relativize(f.toPath()).toString();
			this.locations.add(relFile);
		}
	}

	/**
	 * Lists all files on stack. (also refer to {@link Exception#getMessage()}
	 */
	@Override
	public String getMessage() {
		String str = "A dependency cycle was detected. The file stack is:\n\t";
		str += String.join("\n\t", locations);

		return str;
	}

}
