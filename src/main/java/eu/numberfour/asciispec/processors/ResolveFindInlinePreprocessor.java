package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This processor resolves all <code>{find}</code> directives in the document.<br/>
 * <b>Note</b>: <code>{find}</code> directives within include macros are resolved in the
 * {@link ResolveFindIncludeProcessor}.
 */
public class ResolveFindInlinePreprocessor extends MacroPreprocessor<String> {
	static final String FIND_VARIABLE_KEY = "find";
	static final String FIND_VARIABLE_MATCHER = "\\{(?<VAR>find)\\}[\\s]*(?<FILE>[^\\[\\s]*)";

	@Override
	protected boolean init(Document document) {
		Pattern findvarPattern = Pattern.compile(FIND_VARIABLE_MATCHER);
		registerPattern(FIND_VARIABLE_KEY, findvarPattern);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case FIND_VARIABLE_KEY:
			return getBaseRelativeFileName(document, matcher);
		}
		return fullMatch;
	}

	private String getBaseRelativeFileName(Document document, Matcher findvarMatcher) {
		String fileName = findvarMatcher.group("FILE");
		String baseRelFileName = "{find}" + fileName;
		File findFile = null;
		try {
			findFile = super.searchFile(fileName);
		} catch (FileNotFoundException e) {
			baseRelFileName += " " + error(document, e.getMessage()) + " ";
		} catch (MultipleFileMatchesException e) {
			warn(document, e.getMessage());
			findFile = e.matches.get(0);
		} catch (Exception e) {
			baseRelFileName += " " + error(document, e.getMessage()) + " ";
		}

		if (findFile != null)
			baseRelFileName = super.getBaseRelative(findFile);

		return baseRelFileName;
	}
}
