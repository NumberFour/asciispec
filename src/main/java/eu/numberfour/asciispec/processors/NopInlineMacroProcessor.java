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

import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import eu.numberfour.asciispec.AdocUtils;

/**
 * An inline macro processor that just outputs a texture representation of the macro itself.
 */
public class NopInlineMacroProcessor extends InlineMacroProcessor {

	/**
	 * Creates a new instance with the given name.
	 *
	 * @param name
	 *            the name of this processor
	 */
	public NopInlineMacroProcessor(String name) {
		super(name);
	}

	@Override
	public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
		return AdocUtils.inlineMacroToString(getName(), target, attributes);
	}

}
