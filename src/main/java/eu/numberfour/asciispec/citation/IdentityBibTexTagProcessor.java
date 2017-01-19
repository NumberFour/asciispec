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
 * A processor for BibTeX tag values that does nothing and simply returns the given tag values.
 */
public class IdentityBibTexTagProcessor implements BibTexTagProcessor {
	@Override
	public String processLatexTag(String name, String value) {
		return value;
	}

	@Override
	public String processReplacementTag(String name, String value) {
		return value;
	}
}
