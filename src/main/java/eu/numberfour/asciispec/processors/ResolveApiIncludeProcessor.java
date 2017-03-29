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
package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;

import eu.numberfour.asciispec.findresolver.IgnoreFileException;
import eu.numberfour.asciispec.findresolver.ReplaceIncludeMacroException;


/**
 * This {@link IncludeProcessor} evaluates all include macros in the document
 * whose targets start with <code>{api}</code>.
 * <p>
 * The <code>include::{api}PQN[]</code> macro shall look like an include macro.
 * But it cannot be implemented as a real {@link IncludeProcessor} since it
 * needs to access adoc variables which is not possible inside an
 * {@link IncludeProcessor}. This is only possible in other processors.
 * Consequently, we transform the include macro into an inline macro here. In
 * {@link ResolveApiInlinePreprocessor} the inline macro is then processed.
 */
public class ResolveApiIncludeProcessor extends ResolveIncludeProcessor {
	private static final String INCLUDE_API = "api";

	/**
	 * Constructor
	 */
	public ResolveApiIncludeProcessor() {
		super(INCLUDE_API);
	}

	@Override
	protected File findFile(Document document, Map<String, Object> attributes, File containerFile, String target,
			String line) throws FileNotFoundException, IgnoreFileException, ReplaceIncludeMacroException {

		String newInlineApiMacro = "include:{api}" + target + "[]";
		throw new ReplaceIncludeMacroException(newInlineApiMacro);
	}

	@Override
	protected Map<String, Object> getNewAttributes(Map<String, Object> attributes) {
		return attributes;
	}

}
