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
import eu.numberfour.asciispec.math.MathService;

/**
 * A Prototype MathMl block which uses the SnuggleTex Java Library
 */
@Name("math")
@Contexts(Contexts.CONTEXT_PASS)
@ContentModel(ContentModel.COMPOUND)
public class MathBlockProcessor extends BlockProcessor {

	// TODO: make this configurable
	private final IssueAcceptor issueAcceptor = new IssuePrinter();

	@Override
	public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
		List<String> result = new LinkedList<>();

		String title = AdocUtils.getAttributeAsString(attributes, "title", "");
		if (!title.trim().isEmpty()) {
			String sanitizedTitle = AdocUtils.sanitizeString(title);
			result.add(AdocUtils.createAnchor(sanitizedTitle));
			result.add(AdocUtils.createLink(sanitizedTitle, title));
		}

		final MathService mathService = MathService.get(parent.getDocument());

		List<String> input = reader.readLines();
		List<String> output = mathService.convertBlock(input, issueAcceptor);
		result.addAll(output);

		return createBlock(parent, "open", result, attributes);
	}

}