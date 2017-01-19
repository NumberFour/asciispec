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
package eu.numberfour.asciispec.issue;

import java.util.Objects;

/**
 * Simply prints out all issues.
 */
public class IssuePrinter implements IssueAcceptor {

	@Override
	public Issue accept(Issue issue) {
		System.err.println("asciispec  : " + Objects.requireNonNull(issue).toString());
		return issue;
	}
}
