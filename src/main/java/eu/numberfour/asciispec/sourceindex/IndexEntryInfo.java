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
package eu.numberfour.asciispec.sourceindex;

import java.util.Objects;

/**
 *
 */
public class IndexEntryInfo implements Comparable<IndexEntryInfo> {
	/** The property name. May be empty, but not null. */
	final public String property;
	/**
	 * The delimiter is either '#' or '@' (static). For getter and setters, '<' and '>' is added. May be empty, but not
	 * null.
	 */
	final public String delimiter;
	/** The element name. Usually starting with a type name. */
	final public String element;
	/** The module name also contains the package. */
	final public String module;
	/** The extension of the N4JS file. */
	final public String extension;
	/**
	 * Polyfilling classes are mapped to the PolyfillAware classes. Hence the value of {@link #folder} always refers to
	 * the PolyfillAware class, if one exists.
	 */
	final public String folder;
	/**
	 * Since polyfilling classes are mapped to the PolyfillAware classes, the value of {@link #trueFolder} always refers
	 * to the actual folder where the polyfilling class is located.
	 */
	final public String trueFolder;
	/** The project name */
	final public String project;
	/** The path name. Folders are separated by '/' */
	final public String path;
	/** The repository name */
	final public String repository;
	/** The start line in the N4JS file */
	final public int sourceLine;
	/** The path to the module file */
	final public String packageName;
	/** The module file itself */
	final public String moduleName;
	/** The number of folders of the {@link #packageName} */
	final public int modulePackageCount;
	/** The path to the adoc file. The '/' in the packageName are replaced by '.'. */
	final public String[] adocPathElems;
	/** The {@link #adocPathElems} as one String. */
	final public String adocPath;

	final int offsetStart;
	final int offsetEnd;

	private String toString;
	/**
	 * PQN element hierarchy. The number of hierarchical elements that can be used in a PQN. The folder and the module
	 * file are split into its folders, if possible. Each of these folders is counting as one hierarchical element.
	 * Also, the element is spit into the type and its property, if available.
	 */
	final public String[] hierarchy;

	IndexEntryInfo(
			String repository,
			String path,
			String project,
			String folder,
			String trueFolder,
			String module,
			String extension,
			String element,
			String delimiter,
			String property,
			String packageName,
			String moduleName,
			int sourceLine,
			int modulePackageCount,
			String[] fileNames,
			String fileName,
			int offsetStart,
			int offsetEnd,
			String[] hiercharchy) {

		this.repository = repository;
		this.path = path;
		this.project = project;
		this.folder = folder;
		this.trueFolder = trueFolder;
		this.module = module;
		this.extension = extension;
		this.element = element;
		this.delimiter = delimiter;
		this.property = property;
		this.packageName = packageName;
		this.moduleName = moduleName;
		this.sourceLine = sourceLine;
		this.modulePackageCount = modulePackageCount;
		this.adocPathElems = fileNames;
		this.adocPath = fileName;
		this.offsetStart = offsetStart;
		this.offsetEnd = offsetEnd;
		this.hierarchy = hiercharchy;
	}

	/** Returns the length of {@link #hierarchy} */
	public int getHierarchyDepth() {
		return hierarchy.length;
	}

	/**
	 * Returns the value of the indexed hierarchical element. See {@link #hierarchy}.
	 */
	public String getHierarchyElement(int idx) {
		return hierarchy[idx];
	}

	/**
	 * Returns the relative URL path to the code management system, including the source line.
	 */
	public String getRepoRelativeURL() {
		String url = "";
		url += path + "/";
		url += project + "/";
		url += folder + "/";
		url += module + ".";
		url += extension;

		return url;
	}

	@Override
	public int compareTo(IndexEntryInfo iei) {
		// specKeys are used here for comparison, because they are also
		// used in SpecChangeEntry#compareTo. Since they are equal,
		// both the index file and the adoc file use the same order of
		// module elements.
		return toPQN().compareTo(iei.toPQN());
	}

	/**
	 * Creates a string that is conform to the PQN specification mentioned in IDE-2335.
	 */
	public String toPQN() {
		String s = repository;
		s += ":" + path;
		s += ":" + project;
		s += ":" + folder;
		s += "/" + module;
		s += ":" + element;
		s += delimiter;
		s += property;
		return s;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IndexEntryInfo))
			return false;

		return this.toString().equals(o.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(toString());
	}

	@Override
	public String toString() {
		if (toString == null) {
			toString = repository;
			toString += ":" + path;
			toString += ":" + project;
			toString += ":" + folder;
			if (!folder.equals(trueFolder))
				toString += "(->" + trueFolder + ")";
			toString += ":" + module;
			toString += ":" + element;
			toString += delimiter;
			toString += property;
			toString += ":" + sourceLine;
		}
		return toString;
	}
}
