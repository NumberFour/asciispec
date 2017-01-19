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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a citation reference consisting of a citation key and a list of pages.
 */
public class CitationReference {
	private final String citeKey;
	private final List<String> pages;

	/**
	 * Creates a new citation reference with the given citation key.
	 *
	 * @param citeKey
	 *            the citation key
	 */
	public CitationReference(String citeKey) {
		this(citeKey, Collections.emptyList());
	}

	/**
	 * Creates a new citation reference with the given citation key and page.
	 *
	 * @param citeKey
	 *            the citation key
	 * @param page
	 *            the page
	 */
	public CitationReference(String citeKey, String page) {
		this(citeKey, Collections.singletonList(Objects.requireNonNull(page)));
	}

	/**
	 * Creates a new citation reference with the given citation key and pages.
	 *
	 * @param citeKey
	 *            the citation key
	 * @param pages
	 *            the pages
	 */
	public CitationReference(String citeKey, List<String> pages) {
		this.citeKey = Objects.requireNonNull(citeKey);
		this.pages = Objects.requireNonNull(pages);
	}

	/**
	 * Returns the citation key.
	 *
	 * @return the citation key
	 */
	public final String getCiteKey() {
		return citeKey;
	}

	/**
	 * Returns the pages.
	 *
	 * @return an unmodifiable view of the page list
	 */
	public final List<String> getPages() {
		return Collections.unmodifiableList(pages);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof CitationReference))
			return false;

		CitationReference ref = (CitationReference) obj;
		return Objects.equals(citeKey, ref.citeKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(citeKey);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(citeKey).append("[");

		Iterator<String> pageIt = pages.iterator();
		if (pageIt.hasNext()) {
			result.append(pageIt.next());
			while (pageIt.hasNext())
				result.append(",").append(pageIt.next());
		}

		return result.append("]").toString();
	}
}
