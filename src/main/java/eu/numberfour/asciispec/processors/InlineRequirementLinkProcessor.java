package eu.numberfour.asciispec.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.RequirementReference;

/**
 * An inline preprocessor that creates links to requirements. Supported syntax
 * <code>req:target:version[title="my title",text="my text"]</code>. Attributes are optional an allow definition of
 * custom link text or title. The version element is not utilised at the moment, but is for future functionality that
 * will allow us to detect outdated links to requirements i.e. the actual requirement blocks will have a version as
 * well, so that versions can be compared.
 */

public class InlineRequirementLinkProcessor extends MacroPreprocessor<String> {

	private static final String REQ_KEY = "req";
	private static final Pattern REQ_PATTERN = Pattern.compile(REQ_KEY + ":([^ ]*?)\\[\\]");

	@Override
	public void init(Document document) {
		registerPattern(REQ_KEY, REQ_PATTERN);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();

		switch (key) {
		case REQ_KEY:
			String expression = matcher.group(1);
			RequirementReference reference = RequirementReference.parse(expression);
			if (reference == null) {
				return error(document, "Requirement reference is invalid: '" + fullMatch + "'");
			}

			String target = reference.getId();
			return String.format("link:%s[%s,title=\"%s\"]", "#Req-" + target, target, target);
		}
		return fullMatch;
	}
}
