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

import static eu.numberfour.asciispec.AttributeParser.getAttributeString;

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
 * Although the <code>include::{api}PQN[]</code> macro looks like an include
 * macro, it cannot be implemented using a real {@link IncludeProcessor} since
 * the processor needs to access adoc variables to read the generated API files.
 * However, accessing adoc variables is not possible inside an
 * {@link IncludeProcessor}, but only in other processors. Consequently, we
 * transform the include macro into an inline macro here. In
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

		String newInlineApiMacro = "include:{api}" + target + "[" + getAttributeString(attributes) + "]";
		throw new ReplaceIncludeMacroException(newInlineApiMacro);
	}

	@Override
	protected Map<String, Object> getNewAttributes(Map<String, Object> attributes) {
		return attributes;
	}

}
