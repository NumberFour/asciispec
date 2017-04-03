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
package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

import eu.numberfour.asciispec.findresolver.CannotFindCircularDependenciesException;
import eu.numberfour.asciispec.findresolver.CircularDependencyException;
import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.IgnoreFileException;
import eu.numberfour.asciispec.findresolver.InconsistentUseOfModifiersException;
import eu.numberfour.asciispec.findresolver.ReplaceIncludeMacroException;
import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssuePrinter;

/**
 * The {@link IncludeProcessor} evaluates all include macros which start with a
 * adoc variable like <code>include::{find}file[]</code>. In case included files
 * create a dependency cycle, a warning is issued. The include macro support the
 * FILE_ONCE modifier: <br/>
 * <br/>
 * FILE_ONCE - Only the first match of the given file is included. A warning is
 * issued in case there are more than one matches.
 */
abstract public class ResolveIncludeProcessor extends IncludeProcessor
		implements DirectoriesMixin, ErrorAndWarningsMixin {

	/** Given file is included only once */
	public static final String MODIFIER_FILE_ONCE = "FILE_ONCE";

	private final String adocVarName;
	private final String adocVarNameInBrackets;
	private final FileStackHelper fileSearcher = new FileStackHelper();
	private final Set<File> includedOnceOnlyFiles = new HashSet<>();
	private final IssueAcceptor issueAcceptor = new IssuePrinter();
	private final Set<File> noCircularExceptionsCausingFiles = new HashSet<>();
	private PreprocessorReader reader;
	private Document document;
	private File baseFile;

	/**
	 * Finds the file.
	 *
	 * @param document
	 *            the current document node
	 * @param containerFile
	 *            the file containing the include directive
	 * @param target
	 *            the target string given in the include directive
	 * @param line
	 *            the original line
	 * @return the file. Cannot be null.
	 * @throws FileNotFoundException
	 *             iff the file was not found
	 */
	abstract protected File findFile(Document document, Map<String, Object> attributes, File containerFile,
			String target, String line) throws FileNotFoundException, IgnoreFileException, ReplaceIncludeMacroException;

	/**
	 * Removes all resolver specific attributes
	 *
	 * @param attributes
	 *            original attributes
	 * @return set of attributes which contains only known attributes to this
	 *         class or asciidoctor
	 */
	abstract protected Map<String, Object> getNewAttributes(Map<String, Object> attributes);

	/**
	 * Constructor
	 *
	 * @param adocVarName
	 *            name of the adoc variable at the begin of the target
	 */
	public ResolveIncludeProcessor(String adocVarName) {
		this.adocVarName = adocVarName;
		this.adocVarNameInBrackets = "{" + adocVarName + "}";
		HostPreprocessor.enableIncludeVariable(adocVarName);
	}

	@Override
	public boolean handles(String target) {
		return target.startsWith(adocVarNameInBrackets);
	}

	@Override
	public void process(Document document, PreprocessorReader pReader, String target, Map<String, Object> attributes) {
		this.document = document;
		reader = pReader;
		baseFile = getBaseFile();
		File containerFile = getCurrentFile();

		try {
			fileSearcher.returnToLastLocation(baseFile, containerFile);
		} catch (CannotFindCircularDependenciesException e) {
			File file = e.getFileNotInStack();
			if (!noCircularExceptionsCausingFiles.contains(file))
				warn(document, e.getMessage());
			noCircularExceptionsCausingFiles.add(file);
		}

		target = target.substring(adocVarNameInBrackets.length());
		searchAndInlineFile(document, attributes, containerFile, target);
	}

	/**
	 * Returns the issuAcceptor
	 */
	@Override
	public IssueAcceptor getIssueAcceptor() {
		return issueAcceptor;
	}

	private void searchAndInlineFile(Document document, Map<String, Object> attributes, File containerFile,
			String target) {

		String curLine = "include::" + adocVarNameInBrackets + target + "[" + getAttributeString(attributes) + "]";
		String newLine = "include++::++{" + adocVarName + "\\}" + target + "[" + getAttributeString(attributes) + "]";

		try {
			File file = findFile(document, attributes, containerFile, target, curLine);
			fileSearcher.checkForCircularDependencies(file, getBasedir());

			try {
				checkInconsistentUseOfFileOnceModifier(attributes, file);
			} catch (InconsistentUseOfModifiersException e) {
				warn(document, e.getMessage());
				if (e.hasIgnoreFileException())
					throw e.ignoreFileException;
			}

			String fileName = file.toString();

			Map<String, Object> clearedAttrs = clearAttributes(attributes, MODIFIER_FILE_ONCE);
			clearedAttrs = getNewAttributes(clearedAttrs);
			newLine = "include::" + fileName + "[" + getAttributeString(clearedAttrs) + "]";

			fileSearcher.moveToNewLocation(file);

			String newFileName = containerFile.toString();
			String newPathName = containerFile.getParentFile().toString();
			reader.push_include(newLine, newFileName, newPathName, 1, attributes);
			return;
		} catch (FileNotFoundException e) {
			newLine += " " + error(document, e.getMessage()) + " ";
		} catch (CircularDependencyException e) {
			newLine += " " + error(document, e.getMessage(),
					"Circular dependencies detected. More information in console output.") + " ";
		} catch (ReplaceIncludeMacroException e) {
			newLine = e.replacementString;
		} catch (IgnoreFileException e) {
			return;
		}

		reader.restoreLine(newLine);
	}

	private void checkInconsistentUseOfFileOnceModifier(Map<String, Object> attributes, File file)
			throws IgnoreFileException, InconsistentUseOfModifiersException {

		if (isFileOnce(attributes) && includedOnceOnlyFiles.contains(file)) {
			// normal case #1
			throw new IgnoreFileException();
		}

		if (isFileOnce(attributes) && !includedOnceOnlyFiles.contains(file) && isIncluded(file)) {
			includedOnceOnlyFiles.add(file);
			IgnoreFileException oofaie = new IgnoreFileException();
			throw new InconsistentUseOfModifiersException(oofaie, MODIFIER_FILE_ONCE, getBaseRelative(file).toString());
		}

		if (isFileOnce(attributes) && !includedOnceOnlyFiles.contains(file)) {
			// normal case #2
			includedOnceOnlyFiles.add(file);
		}

		if (!isFileOnce(attributes) && includedOnceOnlyFiles.contains(file)) {
			// non-strict behavior: include this as normal.
			// Forget that the file is used as {@value #MODIFIER_FILE_ONCE}
			// elsewhere
			throw new InconsistentUseOfModifiersException(MODIFIER_FILE_ONCE, getBaseRelative(file).toString());
		}
	}

	/**
	 * Returns true iff the given attributes contain the
	 * {@value #MODIFIER_FILE_ONCE} modifier
	 */
	protected boolean isFileOnce(Map<String, Object> attributes) {
		return attributes.values().contains(MODIFIER_FILE_ONCE);
	}

	/**
	 * Returns true if the given file is already included in the document
	 */
	protected boolean isIncluded(File file) {
		return fileSearcher.isAlreadyIncluded(file);
	}

	private String getAttributeString(Map<String, Object> attributes) {
		Stream<Entry<String, Object>> attrStream = attributes.entrySet().stream();
		Stream<String> attrMap = attrStream.map(e -> e.getKey() + "=\"" + e.getValue() + "\"");
		String str = attrMap.reduce((a, b) -> a + "," + b).orElse("");
		return str;
	}

	/**
	 * Clears the given attributes from the given attribute map
	 */
	@SafeVarargs
	static protected <T> Map<T, Object> clearAttributes(Map<T, Object> attributes, T... removeAttrs) {
		Map<T, Object> newAttrs = new HashMap<>();
		for (Map.Entry<T, Object> entry : attributes.entrySet()) {
			if (!Arrays.asList(removeAttrs).contains(entry.getKey()))
				newAttrs.put(entry.getKey(), entry.getValue());
		}
		return newAttrs;
	}

	@Override
	public PreprocessorReader getReader() {
		return reader;
	}

	@Override
	public Document getDocument() {
		return document;
	}

	/*
	 * Redirect mixin methods.
	 */
	@Override
	public String error(Document document, String consoleMsg, String inlineMsg) {
		return ErrorAndWarningsMixin.super.error(document, consoleMsg, inlineMsg);
	}

	@Override
	public File getCurrentFileBaseRelative() {
		return DirectoriesMixin.super.getCurrentFileBaseRelative();
	}

	@Override
	public int getCurrentLine() {
		return DirectoriesMixin.super.getCurrentLine();
	}
}
