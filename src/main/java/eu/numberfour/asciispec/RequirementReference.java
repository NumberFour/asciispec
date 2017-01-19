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
package eu.numberfour.asciispec;

import java.util.Objects;

/**
 * A requirement reference consists of an ID and a version and is used to store references to optionally versioned
 * requirements.
 */
public class RequirementReference {
	private final String id;
	private final Integer version;

	private RequirementReference(String id, Integer version) {
		this.id = id;
		this.version = version;
	}

	/**
	 * Creates a reference to a requirement by parsing the given string representation, which must be formatted as
	 * follows.
	 *
	 * <pre>
	 * REF ::= ID [: VERSION]
	 * </pre>
	 *
	 * Thereby, the id is a string and the version is a number. If the version is missing or not a valid number, then
	 * the version attribute will be set to <code>null</code>.
	 *
	 * @param str
	 *            the string to parse
	 */
	public static RequirementReference parse(String str) {
		String[] parts = Objects.requireNonNull(str).split(":");
		if (parts.length > 0) {
			String id = parts[0].trim();
			if (id.isEmpty())
				return null;

			if (parts.length == 1)
				return new RequirementReference(id, null);

			if (parts.length == 2) {
				Integer version = AdocUtils.parseInt(parts[1].trim());
				if (version == null || version < 0)
					return null;
				return new RequirementReference(id, version);
			}
		}
		return null;
	}

	/**
	 * Returns the ID of the referenced requirement.
	 *
	 * @return the ID or <code>null</code> if this reference is invalid
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the version of the referenced requirement.
	 *
	 * @return the version or <code>null</code> if this reference is invalid
	 */
	public Integer getVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(id);
		if (version != null)
			result.append(":").append(version);
		return result.toString();
	}
}