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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssuePrinter;

/**
 * Block processor for definition blocks.
 */
@Name("def")
@Contexts(Contexts.CONTEXT_OPEN)
@ContentModel(ContentModel.COMPOUND)
public class DefinitionBlockProcessor extends BlockProcessor {

	// TODO: make this configurable
	private final IssueAcceptor issueAcceptor = new IssuePrinter();

	@Override
	public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
		List<String> input = reader.readLines();
		List<String> output = new LinkedList<>();

		String title = (String) attributes.get("title");
		if (title == null) {
			String message = "Missing definition title";
			issueAcceptor.warn(parent, message);
			title = message.toUpperCase();
		}

		output.add(AdocUtils.createHeader("Definition", title));
		if (!input.isEmpty()) {
			output.add("");
			output.addAll(input);
		}

		attributes.remove("title");
		attributes.put("role", "definition");

		return createBlock(parent, "open", output, attributes);
	}
}
