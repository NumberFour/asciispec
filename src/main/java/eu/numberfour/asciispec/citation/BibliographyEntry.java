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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an entry from a BibTeX file.
 *
 * <p>
 * Note that tag names are not case sensitive and are therefore converted to lower case when they are added using
 * {@link #addTag(String, String)}. The same applies for the type string passed to the constructor.
 * </p>
 */
public class BibliographyEntry {
	private final String type;
	private final String citeKey;
	private final Map<String, String> tags;

	/**
	 * Creates a new entry with the given type and citation key. The given type string is converted to lower case.
	 *
	 * @param type
	 *            the entry type
	 * @param citeKey
	 *            the citation key
	 */
	public BibliographyEntry(String type, String citeKey) {
		this.type = Objects.requireNonNull(type.toLowerCase());
		this.citeKey = citeKey;
		this.tags = new HashMap<>();
	}

	/**
	 * Creates a new entry with the given type, the given citation key, and the given tags. The given type string is
	 * converted to lower case.
	 *
	 * @param type
	 *            the entry type
	 * @param citeKey
	 *            the citation key
	 * @param tags
	 *            the tags
	 */
	BibliographyEntry(String type, String citeKey, Map<String, String> tags) {
		this(type, citeKey);
		addTags(tags);

	}

	/**
	 * Adds the tags from the given map to this entry. The tag names will be converted to lower case
	 *
	 * @param tagsToAdd
	 *            the tags to add.
	 */
	public void addTags(final Map<String, String> tagsToAdd) {
		Objects.requireNonNull(tags);
		for (Map.Entry<String, String> tag : tagsToAdd.entrySet())
			addTag(tag.getKey(), tag.getValue());
	}

	/**
	 * Adds a tag with the given name and value to this entry. The tag name will be converted to lower case.
	 *
	 * @param name
	 *            the tag name
	 * @param value
	 *            the value
	 */
	public void addTag(String name, String value) {
		if (name.trim().isEmpty())
			throw new IllegalArgumentException("Tag name must not be blank");
		tags.put(name.toLowerCase(), value);
	}

	/**
	 * Returns the type string. Note that the type string passed to the constructor is converted to lower case.
	 *
	 * @return the type string
	 */
	public final String getType() {
		return type;
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
	 * Indicate whether this entry has the given citation key.
	 *
	 * @param aCiteKey
	 *            the citation key to check
	 * @return <code>true</code> if this entry has the given citation key and <code>false</code> otherwise
	 */
	public boolean hasCiteKey(String aCiteKey) {
		Objects.requireNonNull(aCiteKey);
		return citeKey.equals(aCiteKey);
	}

	/**
	 * Returns an unmodifiable view of the tags that were added to this entry.
	 *
	 * @return the tags
	 */
	public final Map<String, String> getTags() {
		return Collections.unmodifiableMap(tags);
	}

	/**
	 * Returns the value of the tag with the given name. Note that tag names are not case sensitive.
	 *
	 * @param name
	 *            the name of the tag to retrieve
	 * @param defaultValue
	 *            the value to return if no tag with the given name exists
	 * @return the value of the tag with the given name, or the default value if no such tag exists
	 */
	public final String getTag(String name, String defaultValue) {
		final String value = tags.get(name.toLowerCase());
		if (value == null)
			return defaultValue;
		return value;
	}

	/**
	 * Compares two collections of entries. The entries are compared using the {@link #equals(BibliographyEntry)}
	 * method.
	 *
	 * @param lhs
	 *            the first collection
	 * @param rhs
	 *            the second collection
	 * @return <code>true</code> if both collections are <code>null</code> or if both collections contain equal entries
	 *         in the same order, and <code>false</code> otherwise
	 */
	public static boolean equals(Collection<BibliographyEntry> lhs, Collection<BibliographyEntry> rhs) {
		if (lhs == null && rhs == null)
			return true;
		if (lhs == null || rhs == null)
			return false;
		if (lhs.size() != rhs.size())
			return false;

		Iterator<BibliographyEntry> lhsIt = lhs.iterator();
		Iterator<BibliographyEntry> rhsIt = rhs.iterator();
		while (lhsIt.hasNext() && rhsIt.hasNext()) {
			if (!lhsIt.next().equals(rhsIt.next()))
				return false;
		}

		return true;
	}

	/**
	 * Compares this entry to the given entry.
	 *
	 * <p>
	 * Note that this method intentionally does not override {@link #equals(Object)}, since that would require us to
	 * override {@link #hashCode()} also.
	 * </p>
	 *
	 * @param other
	 *            the entry to compare to
	 * @return <code>true</code> if the given entry is not <code>null</code> and all fields of this entry are equal to
	 *         the fields of the given entry and <code>false</code> otherwise
	 */
	public boolean equals(BibliographyEntry other) {
		if (other == null)
			return false;

		return type.equals(other.type) && citeKey.equals(other.citeKey) && tags.equals(other.tags);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("@").append(type).append(" { ").append(citeKey);

		for (Map.Entry<String, String> tag : tags.entrySet()) {
			result.append(",\n");
			result.append(tag.getKey()).append(" = ").append("\"").append(tag.getValue()).append("\"");
		}

		result.append("\n}\n");
		return result.toString();
	}
}
