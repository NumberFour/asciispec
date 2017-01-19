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

import java.io.File;
import java.util.List;

import eu.numberfour.asciispec.sourceindex.SourceIndexProcessor.LineInfo;

/**
 * Builds {@link IndexEntryInfo}s
 */
public class IndexEntryInfoFactory {

	/** Delimiter in index.idx files. */
	static final String DELIMITER = "#";
	static final String NO_PACKAGE = "NO_PACKAGE";

	/**
	 * Returns a {@link SourceEntry}. Used by the {@link IndexFileParser}.
	 */
	static public IndexEntryInfo create(List<String> nameStack, LineInfo info) {
		String property = info.property; // might be null
		String delimiter = info.delimiter; // might be null
		String element = nameStack.get(0);
		String moduleNameFileAndExtension = nameStack.get(1);
		String packageName = nameStack.get(2).replace(".", "/");
		String projectAndSourceFolder = nameStack.get(3);
		String repositoryAndPath = nameStack.get(4);

		String[] repositoryAndPathArr = repositoryAndPath.split("#");
		String[] projectAndSourceFolderArr = projectAndSourceFolder.split("#");
		String[] moduleNameFileAndExtensionArr = moduleNameFileAndExtension.split("\\.");

		String repository = repositoryAndPathArr[0];
		String path = repositoryAndPathArr[1].replace(".", "/");
		String project = projectAndSourceFolderArr[0];
		String folder = projectAndSourceFolderArr[1].replace(".", "/");
		String moduleName = moduleNameFileAndExtensionArr[0];
		String extension = moduleNameFileAndExtensionArr[1];
		String module = packageName + "/" + moduleName;
		String trueFolder = folder;
		if (info.hasTrueFolder) {
			/*
			 * currently not used
			 * @formatter:off
			String tRepo = info.trueRepository;
			String tPath = info.truePath.replace("\\.", "/");
			String tProj = info.trueProject;
			 * @formatter:on
			 */
			String tFold = info.trueFolder.replace(".", "/");
			trueFolder = tFold;
		}

		int sourceLine = info.lineNumber;
		int modulePackageCount = countFolderDepth(module);
		String[] hiercharchy = getHierarchy(repository, path, project, folder, module, element, delimiter, property);
		String[] fileNames = getFileNames(repository, path, project, folder, packageName, moduleName);
		String fileName = getFileName(fileNames);

		int offsetStart = info.offsetStart;
		int offsetEnd = info.offsetEnd;

		IndexEntryInfo siEntry = new IndexEntryInfo(
				repository,
				path,
				project,
				folder,
				trueFolder,
				module,
				extension,
				element,
				delimiter,
				property,
				packageName,
				moduleName,
				sourceLine,
				modulePackageCount,
				fileNames,
				fileName,
				offsetStart,
				offsetEnd,
				hiercharchy);

		return siEntry;
	}

	/**
	 * Constructor for test purposes. <br/>
	 * <b>Attention!</b> The TQN (testing qualified name) is delimited by <i>single colons</i> only!
	 */
	static public IndexEntryInfo create(String tqn) {
		String[] tqnArray = tqn.split(":");

		String repository = tqnArray[0];
		String path = tqnArray[1];
		String project = tqnArray[2];
		String folder = tqnArray[3];
		String moduleAndExtension = tqnArray[4];
		String elemAndProp = tqnArray[5];

		String[] moduleAndExtensionArr = moduleAndExtension.split("\\.");
		String module = moduleAndExtensionArr[0];
		String extension = moduleAndExtensionArr[1];

		String[] elemAndPropArr = AbstractPQNParser.unquoteElemAndProp(elemAndProp);
		String element = elemAndPropArr[0];
		String delimiter = elemAndPropArr[1];
		String property = elemAndPropArr[2];
		int sourceLine = -1;
		int offsetStart = -1;
		int offsetEnd = -1;
		if (tqnArray.length > 6) {
			sourceLine = Integer.valueOf(tqnArray[6]);
			offsetStart = Integer.valueOf(tqnArray[7]);
			offsetEnd = Integer.valueOf(tqnArray[8]);
		}
		String trueFolder = folder;
		if (tqnArray.length > 9) {
			trueFolder = tqnArray[12];
		}

		String moduleName = getModuleName(module);
		String packageName = getPackageName(module);

		int modulePackageCount = countFolderDepth(module);
		String[] hiercharchy = getHierarchy(repository, path, project, folder, module, element, delimiter, property);
		String[] fileNames = getFileNames(repository, path, project, folder, packageName, moduleName);
		String fileName = getFileName(fileNames);

		IndexEntryInfo siEntry = new IndexEntryInfo(
				repository,
				path,
				project,
				folder,
				trueFolder,
				module,
				extension,
				element,
				delimiter,
				property,
				packageName,
				moduleName,
				sourceLine,
				modulePackageCount,
				fileNames,
				fileName,
				offsetStart,
				offsetEnd,
				hiercharchy);

		return siEntry;
	}

	private static String[] getFileNames(String repo, String path, String proj, String srcFld, String pckgNme,
			String modNme) {
		String[] fNames = new String[4];
		fNames[0] = repo + DELIMITER + path.replace("/", ".");
		fNames[1] = proj + DELIMITER + srcFld.replace("/", ".");
		fNames[2] = pckgNme.replace("/", ".");
		fNames[3] = modNme + ".adoc";
		return fNames;
	}

	private static String[] getHierarchy(String repo, String path, String proj, String srcFld, String module,
			String element, String delimiter, String property) {

		String[] srcFlds = trim(srcFld).split("/");
		String[] mdlFlds = trim(module).split("/");
		int depth = 3 + srcFlds.length + mdlFlds.length + 1;
		if (delimiter != null && !delimiter.isEmpty())
			depth += 2;
		String[] elems = new String[depth];

		int i = 0;
		elems[i++] = repo;
		elems[i++] = path;
		elems[i++] = proj;

		for (int tmp = i; i < tmp + srcFlds.length; i++)
			elems[i] = srcFlds[i - tmp];

		for (int tmp = i; i < tmp + mdlFlds.length; i++)
			elems[i] = mdlFlds[i - tmp];

		elems[i++] = element;

		if (delimiter != null && !delimiter.isEmpty()) {
			elems[i++] = delimiter;
			elems[i++] = property;
		}

		return elems;
	}


	/** Extracts the package name from a module. */
	private static String getPackageName(String module) {
		String tPackageName = "";
		if (module.contains("/")) {
			int moduleNameStart = module.lastIndexOf("/");
			tPackageName = module.substring(0, moduleNameStart);
		}

		tPackageName = trim(tPackageName);
		if (tPackageName.isEmpty())
			tPackageName = NO_PACKAGE;

		return tPackageName;
	}

	/** Extracts the module name from a module. */
	private static String getModuleName(String module) {
		String tModuleName = module;
		if (module.contains("/")) {
			int moduleNameStart = module.lastIndexOf("/");
			tModuleName = module.substring(moduleNameStart);
		}

		tModuleName = trim(tModuleName);
		return tModuleName;
	}

	/** Concatenates a file name from a path that is given as a list of folders, terminated by an adoc file name. */
	private static String getFileName(String[] fileNames) {
		String fName = fileNames[0];
		fName += File.separator + fileNames[1];
		if (fileNames[2] != null && !fileNames[2].isEmpty())
			fName += File.separator + fileNames[2];
		fName += File.separator + fileNames[3];
		return fName;
	}

	/** Cuts off '/' at the beginning and end of a String. */
	private static String trim(String s) {
		if (s.startsWith("/"))
			s = s.substring(1);
		if (s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		return s;
	}

	/**
	 * Returns the number of packages in the given module name {@code module}.
	 */
	private static int countFolderDepth(String module) {
		int count = countInString('/', module);

		if (module.startsWith("/"))
			count--;
		if (module.endsWith("/"))
			count--;

		return count + 1;
	}

	/**
	 * Returns the number of occurrences for a given char {@code ch} in the given string {@code text}.
	 */
	private static int countInString(char ch, String text) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ch)
				count++;
		}

		return count;
	}
}
