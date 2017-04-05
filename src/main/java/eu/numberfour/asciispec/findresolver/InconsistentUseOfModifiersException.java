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

import eu.numberfour.asciispec.processors.ResolveFindIncludeProcessor;

/**
 * The use of some include modifiers can lead to inconsistencies. When such an inconsistency is found the user is
 * informed using this exception.
 */
public class InconsistentUseOfModifiersException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** In case the file has the {@link ResolveFindIncludeProcessor#MODIFIER_FILE_ONCE} modifier */
	public final IgnoreFileException ignoreFileException;
	/** The name of the modifier */
	public final String modifierName;
	/** The file name which is included */
	public final String fileName;

	/**
	 * @param ife
	 *            is set iff the file is already included
	 * @param modifierName
	 *            the name of the modifier
	 * @param fileName
	 *            the file that is included
	 */
	public InconsistentUseOfModifiersException(IgnoreFileException ife, String modifierName, String fileName) {
		this.ignoreFileException = ife;
		this.modifierName = modifierName;
		this.fileName = fileName;
	}

	/**
	 * @param modifierName
	 *            the name of the modifier
	 * @param fileName
	 *            the file that is included
	 */
	public InconsistentUseOfModifiersException(String modifierName, String fileName) {
		this(null, modifierName, fileName);
	}

	/**
	 * @return true if {@link #ignoreFileException} is not null
	 */
	public boolean hasIgnoreFileException() {
		return ignoreFileException != null;
	}

	@Override
	public String getMessage() {
		String msg = "Inconsistent use of modifier " + modifierName;
		msg += " at file '" + fileName + "'";
		return msg;
	}
}
