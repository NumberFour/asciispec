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
	static final String FINDROOT_VARIABLE_KEY = "findroot";
	static final String FINDROOT_VARIABLE_MATCHER = ":findroot:";

	@Override
	public void init(Document document) {
		Pattern findvarPattern = Pattern.compile(FIND_VARIABLE_MATCHER);
		registerPattern(FIND_VARIABLE_KEY, findvarPattern);
		Pattern findrootPattern = Pattern.compile(FINDROOT_VARIABLE_MATCHER);
		registerPattern(FINDROOT_VARIABLE_KEY, findrootPattern);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case FIND_VARIABLE_KEY:
			return getBaseRelativeFileName(document, matcher);
		case FINDROOT_VARIABLE_KEY:
			return error(document, "Variable 'findroot' is only allowed as a command line argument.");
		}
		return fullMatch;
	}

	/**
	 * Returns the relative path to the given target. A relative path is
	 * necessary since html might get generated which will also be used in www
	 * scenarios. Absolute paths would break this scenario.
	 */
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

		if (findFile != null) {
			baseRelFileName = super.getBaseRelative(findFile).toString();
		}

		return baseRelFileName;
	}

}
