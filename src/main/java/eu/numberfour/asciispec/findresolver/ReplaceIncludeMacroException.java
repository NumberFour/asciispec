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
package eu.numberfour.asciispec.findresolver;

/**
 * Is used when a file should not be included.
 */
public class ReplaceIncludeMacroException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	//

	final public String replacementString;

	public ReplaceIncludeMacroException(String replacementString) {
		this.replacementString = replacementString;
	}

}
