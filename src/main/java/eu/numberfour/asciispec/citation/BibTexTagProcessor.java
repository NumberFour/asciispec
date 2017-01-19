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
package eu.numberfour.asciispec.citation;

/**
 * Processes tags from a bibtex bibliography entry. There are four types of tags in BibTeX entries:
 *
 * <ul>
 * <li><b>LaTeX</b>: Enclosed in curly braces, might contain LaTeX code and must therefore be processed</li>
 * <li><b>Replacement</b>: Alphanumeric string that contains at least one non-numeric character</li>
 * <li><b>Number</b>: Sequence of numeric characters, no further processing</li>
 * <li><b>String</b>: Enclosed in double quotation marks, no further processing</li>
 * </ul>
 *
 * Thereby, two types of tags can be processed: LaTeX tags, which have their values enclosed in curly braces in the
 * BibTeX file, and replacement tags, which have a value that is not a numeric string and that is neither enclosed in
 * double quotation marks nor in curly braces.
 */
public interface BibTexTagProcessor {
	/**
	 * Process a LaTeX tag value. Note that the enclosing braces have already been stripped, even if there are multiple
	 * enclosing braces, e.g. <code>{{This is the value}}</code>.
	 *
	 * @param name
	 *            the tag name
	 * @param value
	 *            the tag value to process
	 * @return the processed tag value
	 */
	public String processLatexTag(String name, String value);

	/**
	 * Process a replacement tag value.
	 *
	 * @param name
	 *            the tag name
	 * @param value
	 *            the tag value to process
	 * @return the processed tag value
	 */
	public String processReplacementTag(String name, String value);
}
