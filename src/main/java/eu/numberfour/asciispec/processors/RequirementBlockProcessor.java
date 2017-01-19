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

import java.util.ArrayList;
import java.util.HashMap;
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
import eu.numberfour.asciispec.issue.IssueCollector;
import eu.numberfour.asciispec.issue.IssuePrinter;

/**
 * Block processor for requirement blocks.
 */
@Name("req")
@Contexts(Contexts.CONTEXT_OPEN)
@ContentModel(ContentModel.COMPOUND)
public class RequirementBlockProcessor extends BlockProcessor {

	// TODO: make this configurable
	private final IssueAcceptor issueAcceptor = new IssuePrinter();

	@Override
	public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
		IssueCollector issues = new IssueCollector(issueAcceptor);

		final String id = getRequirementId(attributes);
		final String title = getRequirementTitle(attributes);

		final String versionStr = getRequirementVersion(attributes);
		final Integer version = versionStr != null ? AdocUtils.parseInt(versionStr) : null;

		if (id == null)
			issues.error(parent, "Requirement ID is missing");
		if (title == null)
			issues.error(parent, "Requirement title is missing");
		if (versionStr == null)
			issues.error(parent, "Requirement version is missing");
		else if (version == null)
			issues.error(parent, "Requirement version is invalid: '" + versionStr + "'");
		else if (version < 0)
			issues.error(parent, "Requirement version must be a nonnegative integer, but is " + version.toString());

		List<String> input = reader.readLines();
		List<String> output = new ArrayList<>();

		// Build the header
		String actualTitle = title != null ? title : "MISSING TITLE";
		String actualId = id != null ? id : "MISSING ID";
		String sanitizedId = id != null ? "Req-" + actualId : AdocUtils.sanitizeString(actualId);
		StringBuilder header = new StringBuilder();
		AdocUtils.appendHeader(header, "Req. " + actualId, actualTitle, sanitizedId);

		if (version != null && version >= 0)
			header.append(" (ver. ").append(version).append(")");
		else
			header.append(" (").append("INVALID VERSION").append(")");

		output.add(header.toString());
		issues.appendTo(output);

		if (!input.isEmpty()) {
			output.add("");
			output.addAll(input);
		}

		attributes.remove("id");
		attributes.remove("title");
		attributes.put("role", "requirement");

		return createBlock(parent, "open", output, attributes, new HashMap<>());
	}

	private String getRequirementId(Map<String, Object> attributes) {
		return AdocUtils.getAttributeAsString(attributes, "id", null);
	}

	private String getRequirementTitle(Map<String, Object> attributes) {
		return AdocUtils.getAttributeAsString(attributes, "title", null);
	}

	private String getRequirementVersion(Map<String, Object> attributes) {
		return AdocUtils.getAttributeAsString(attributes, "version", null);
	}
}
