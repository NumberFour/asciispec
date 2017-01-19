package eu.numberfour.asciispec.processors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.ParseException;

/**
 * An inline macro processor that creates links to articles on a wiki. Processing requires configuration. Configuration
 * has the syntax <code>:cwiki_def:path;page;icon;title</code>. The wiki macro itself has the syntax
 * <code>cwiki:target[optional attrs]</code>.
 */

public class InlineWikiLinkProcessor extends MacroPreprocessor<String> {

	private static final String CWIKI_KEY = "cwiki";
	/** supports nested inline macros up to three levels */
	private static final Pattern CWIKI_PATTERN = Pattern
			.compile("cwiki:(?<TARGET>[^\\[\\]]*)(\\[(?<ATTRS>([^\\[\\]]*|\\[([^\\[\\]]*|\\[[^\\[\\]]*\\])*\\])*)\\])");

	private static final String CONFIG_KEY = "cwiki_def";
	private static final Pattern CONFIG_PATTERN = Pattern
			.compile(":cwiki_def:\\s*(.*?)\\s*;\\s*(.*?)\\s*;\\s*(.*?)\\s*;\\s*(.*)");

	private static final String VERIFY_CONFIG_KEY = "cwiki_def_loose";
	private static final Pattern RELAXED_CONFIG_PATTERN = Pattern.compile(":cwiki_def:.*");

	private String pathTemplate, pageTemplate, iconTemplate, titleTemplate;
	private boolean isConfigured = false;

	@Override
	protected boolean init(Document document) {
		registerPattern(CONFIG_KEY, CONFIG_PATTERN);
		registerPattern(VERIFY_CONFIG_KEY, RELAXED_CONFIG_PATTERN);
		registerPattern(CWIKI_KEY, CWIKI_PATTERN);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case CONFIG_KEY:
			configure(matcher);
			return fullMatch;
		case VERIFY_CONFIG_KEY:
			isConfigured = verifyConfiguration();
			if (!isConfigured) {
				return error(document, "Invalid wiki configuration: " + fullMatch);
			}
			return fullMatch;
		case CWIKI_KEY:
			if (!isConfigured) {
				return error(document, "Missing wiki configuration, skipping macro: " + fullMatch);
			}
			try {
				return createLink(matcher);
			} catch (Exception e) {
				return error(document, "Could not parse wiki link: " + fullMatch);
			}
		}
		return fullMatch;
	}

	private String createLink(Matcher matcher) throws ParseException {
		String target = matcher.group("TARGET");
		String attrStr = matcher.group("ATTRS");

		Map<String, Object> rawAttrMap = new HashMap<>();
		rawAttrMap.put("text", attrStr);
		String linkTitle = AdocUtils.getRawAttributeAsString(rawAttrMap, "title", 0, target);
		// the following line removes all occurrences of []-blocks
		// since they are not supported in the image macro
		String imageTitle = linkTitle.replaceAll("((?<=[^\\\\])\\[|^\\[)((.*[^\\\\])\\]|\\])", "");

		String url = AdocUtils.transformVariable(pathTemplate, "PATH", target);
		if (target.matches("\\d+")) {
			url = AdocUtils.transformVariable(pageTemplate, "PAGE_ID", target);
		}

		String linkText = AdocUtils.transformVariable(titleTemplate, "TITLE", imageTitle);

		String imgLink = AdocUtils.createImageWithLink(iconTemplate, imageTitle, url);
		String txtLink = AdocUtils.createLinkWithTitle(url, linkText, linkTitle);
		return imgLink + txtLink;
	}

	private void configure(Matcher matcher) {
		pathTemplate = matcher.group(1);
		pageTemplate = matcher.group(2);
		iconTemplate = matcher.group(3);
		titleTemplate = matcher.group(4);
		isConfigured = true;
	}

	private boolean verifyConfiguration() {
		return pathTemplate != null &&
				pageTemplate != null &&
				iconTemplate != null &&
				titleTemplate != null;
	}
}
