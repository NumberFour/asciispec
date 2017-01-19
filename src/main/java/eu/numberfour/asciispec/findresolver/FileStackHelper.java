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
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * The {@link FileStackHelper} does the following:
 * <ul>
 * <li>Can search for files: {@link #searchRelativeTo(String, File, Path)}</li>
 * <li>Keeps track of included files using {@link #includedFiles}</li>
 * <li>Can check for circular dependencies (required: full file stack)</li>
 * <li>Also works with partial file stacks (result: can not find circular dependencies)</li>
 * </ul>
 */
public class FileStackHelper {
	final private Stack<File> locationFileStack = new Stack<>();
	final private Set<File> includedFiles = new HashSet<>();

	/**
	 * Searches for a file with the given file name. The search looks for the file in the current location and in every
	 * parent directory. The file name can include directories. The returned file is absolute.
	 *
	 * @return absolute file
	 */
	public File search(String fileName, Path basedir) throws FileNotFoundException, MultipleFileMatchesException {
		return searchRelativeTo(fileName, getCurrentFile(), basedir);
	}

	/**
	 * Pushes the new location to the file stack. In case the given file is not a directory, its parent is used instead.
	 * The file has to be a file and absolute. A {@link CircularDependencyException} is thrown when the given new
	 * location is already present in the current file stack.
	 */
	public void moveToNewLocation(File newLocation) {
		if (!newLocation.isAbsolute())
			throw new IllegalArgumentException("New location must be absolute");

		if (!newLocation.isFile())
			throw new IllegalArgumentException("New location must be a file");

		includedFiles.add(newLocation);
		locationFileStack.push(newLocation);
	}

	/**
	 * Checks if a new location can lead to a dependency cycle. Throws an exception when a dependency cycle would be
	 * created.
	 */
	public void checkForCircularDependencies(File newLocation, Path basedir) throws CircularDependencyException {
		if (!newLocation.isAbsolute())
			throw new IllegalArgumentException("New location must be absolute");

		if (!newLocation.isFile())
			throw new IllegalArgumentException("New location must be a file");

		if (locationFileStack.contains(newLocation))
			throw new CircularDependencyException(locationFileStack, basedir);
	}

	/**
	 * Pops location from the stack until the last location is found. Throws a
	 * {@link CannotFindCircularDependenciesException} when the given lastLocation file cannot be found on the
	 * stack.<br/>
	 * <br/>
	 * Note: This implementation takes into account that the location stack can lack some locations. This can happen
	 * when an include does not use the {find} macro. If it happens, the whole stack is emptied and the exception
	 * mentioned before is thrown.
	 *
	 * @param baseFile
	 *            the base/master adoc file
	 * @param lastLocation
	 *            the file to return to
	 *
	 */
	public void returnToLastLocation(File baseFile, File lastLocation) throws CannotFindCircularDependenciesException {
		includedFiles.add(lastLocation);

		// init
		if (locationFileStack.isEmpty()) {
			locationFileStack.push(lastLocation);
		}

		// drop until found in stack
		while (!locationFileStack.isEmpty() && !locationFileStack.peek().equals(lastLocation))
			locationFileStack.pop();

		if (locationFileStack.isEmpty() || !locationFileStack.contains(baseFile)) {
			locationFileStack.push(lastLocation);
			throw new CannotFindCircularDependenciesException(lastLocation);
		}
	}

	/**
	 * Returns the current file
	 */
	public File getCurrentFile() {
		return locationFileStack.lastElement();
	}

	/**
	 * Checks if the given file was already included in the document.
	 */
	public boolean isAlreadyIncluded(File file) {
		return includedFiles.contains(file);
	}

	/**
	 * Searches for a file with the given file name. The search looks for the file in the current location and in every
	 * parent directory. The file name can include directories. The returned file is absolute.
	 *
	 * @return absolute file
	 */
	public static File searchRelativeTo(String fileName, File containerFile, Path path)
			throws FileNotFoundException, MultipleFileMatchesException {

		Path curPath = containerFile.toPath();
		List<File> matches = new LinkedList<>();
		while (curPath != null) {
			Path filePath = curPath.resolve(fileName);
			if (filePath.toFile().exists())
				matches.add(filePath.toFile());
			curPath = curPath.getParent();
		}

		if (matches.isEmpty())
			throw new FileNotFoundException("File '" + fileName + "' could not be found");

		if (matches.size() > 1)
			throw new MultipleFileMatchesException(fileName, matches, path);

		return matches.get(0);
	}

}
