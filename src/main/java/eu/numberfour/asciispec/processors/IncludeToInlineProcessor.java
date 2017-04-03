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

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * This {@link IncludeProcessor} evaluates all include macros in the document
 * whose targets start with the given <code>variableName</code>.
 * <p>
 * Although (e.g.) the <code>include::{api}PQN[]</code> macro looks like an
 * include macro, it cannot be implemented using a real {@link IncludeProcessor}
 * since the processor needs to access adoc variables to read the generated API
 * files. However, accessing adoc variables is not possible inside an
 * {@link IncludeProcessor}, but only in other processors like
 * {@link Preprocessor}s. Consequently, we transform the include macro into an
 * inline macro here. In {@link ResolveApiInlinePreprocessor} the inline macro
 * is then processed.
 */
abstract public class IncludeToInlineProcessor extends IncludeProcessor {
	private final String variableName;

	/**
	 * Constructor
	 */
	public IncludeToInlineProcessor(String variableName) {
		this.variableName = variableName;
		HostPreprocessor.enableIncludeVariable(variableName);
	}

	@Override
	public boolean handles(String target) {
		return target.startsWith("{" + variableName + "}");
	}

	@Override
	public void process(Document document, PreprocessorReader pReader, String target, Map<String, Object> attributes) {
		String newInlineMacro = "include:" + target + "[" + getAttributeString(attributes) + "]";

		pReader.restoreLine(newInlineMacro);
	}

}
