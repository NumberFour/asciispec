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

import java.util.List;
import java.util.Objects;

/**
 * A database of BibTeX entries.
 */
public class BibliographyDatabase {
	private final List<BibliographyEntry> entries;

	/**
	 * Creates a new database with the given BibTeX entries.
	 *
	 * @param entries
	 *            the entries for this database
	 */
	public BibliographyDatabase(List<BibliographyEntry> entries) {
		this.entries = Objects.requireNonNull(entries);
	}

	/**
	 * Returns the first BibTeX entry for with the given citation key.
	 *
	 * @param citeKey
	 *            the citation key to search for
	 * @return the first BibTeX entry with the given citation key or <code>null</code> if no such entry could be found
	 */
	public BibliographyEntry findByCiteKey(String citeKey) {
		for (BibliographyEntry entry : entries) {
			if (entry.hasCiteKey(citeKey))
				return entry;
		}
		return null;
	}
}
