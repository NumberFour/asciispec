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

/**
 * Gets thrown when an index could not be found.
 */
public class NotInSourceIndexExcpetion extends Exception {

	NotInSourceIndexExcpetion() {
	}

	NotInSourceIndexExcpetion(String msg) {
		super(msg);
	}
}
