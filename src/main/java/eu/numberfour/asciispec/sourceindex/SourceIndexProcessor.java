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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.numberfour.asciispec.ParseException;

/**
 * The {@link SourceIndexProcessor} handles string lines read by the {@link IndexFileParser}. Information form the
 * parser is passed using the {@link LineInfo} class.
 */
class SourceIndexProcessor {

	static class LineInfo {
		int line;
		int column;
		int tabCount;
		String partialLocation;
		String delimiter;
		String property;
		boolean validNumbers;
		int lineNumber;
		int offsetStart;
		int offsetEnd;
		boolean hasTrueFolder;
		String trueRepository;
		String truePath;
		String trueProject;
		String trueFolder;
	}

	private final List<String> nameStack = new ArrayList<>();
	private SourceIndexDatabase siDatabase;
	private int tabCountLast = -1;

	void init() {
		siDatabase = new SourceIndexDatabase();
		tabCountLast = -1;
	}

	SourceIndexDatabase getDatabase() {
		return siDatabase;
	}

	/**
	 * Processes line by line from the index file.
	 *
	 * @throws ParseException
	 *             if tabs are inconsistent or if the index file contains duplicates.
	 */
	void processLine(LineInfo lineInfo) throws ParseException {
		Objects.requireNonNull(siDatabase);

		int tabDiff = lineInfo.tabCount - tabCountLast;
		if (tabDiff > 1)
			throw new ParseException(lineInfo.line, lineInfo.column, "Too many tabs");

		for (int i = tabDiff; i <= 0; i++)
			nameStack.remove(0);

		nameStack.add(0, lineInfo.partialLocation);

		if (lineInfo.validNumbers) {
			IndexEntryInfo siEntry = IndexEntryInfoFactory.create(nameStack, lineInfo);
			try {
				siDatabase.put(siEntry);
			} catch (AlreadyInsertedException e) {
				throw new ParseException(lineInfo.line, lineInfo.column, "Index contains duplicates");
			}
		}

		tabCountLast = lineInfo.tabCount;
	}

}
