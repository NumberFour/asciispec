package eu.numberfour.asciispec.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.math.MathService;

/**
 * A preprocessor that processes MathMl. It supports a shorthand syntax <code>$expression$</code>, full syntax
 * <code>math:expression[]</code>, and a mixture of both <code>math:$expression$[]</code>. The preprocessor is also
 * responsible for unescaping the dollar signs, which are written into the source document to tell this preprocessor to
 * ignore those. For example <code>\$not math\$</code> will result in <code>$not math$</code> after the source is
 * processed by this preprocessor.
 */
public class InlineMathPreprocessor extends MacroPreprocessor<String> {

	private static final String MATH_KEY = "math";
	private static final String MATH_FULL_CONTENT_LABEL = "fullSyntax";
	private static final String MATH_SHORT_CONTENT_LABEL = "shortSyntax";
	private static final Pattern MATH_PATTERN = Pattern.compile(String.format(
			"(%s:(?<%s>.*?)\\[[^\\]]*\\])|(?<=[^\\\\\\$]|^)(\\$(?<%s>[^\\$]*?[^\\\\\\$])\\$)",
			MATH_KEY, MATH_FULL_CONTENT_LABEL, MATH_SHORT_CONTENT_LABEL));

	private static final String MATH_UNESCAPE_KEY = "mathUnescape";
	private static final Pattern MATH_UNESCAPE_PATTERN = Pattern.compile("(\\\\\\$)");

	@Override
	protected boolean init(Document document) {
		registerPattern(MATH_KEY, MATH_PATTERN);
		registerPattern(MATH_UNESCAPE_KEY, MATH_UNESCAPE_PATTERN);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case MATH_KEY:
			try {
				final MathService mathService = MathService.get(document);
				return mathService.convertInline(extractMathTerm(matcher));
			} catch (IllegalArgumentException e) {
				return error(document, e.getMessage());
			}
		case MATH_UNESCAPE_KEY:
			return fullMatch.replace("\\$", "$");
		}
		return fullMatch;
	}

	private String extractMathTerm(Matcher matcher) {
		// try to get the full syntax expression, if failed, try to get the shorthand syntax expression
		String mathTerm = matcher.group(MATH_FULL_CONTENT_LABEL) != null ? matcher.group(MATH_FULL_CONTENT_LABEL)
				: matcher.group(MATH_SHORT_CONTENT_LABEL);
		// special treatment of the full syntax in case it was combined with short syntax (due existing spec usages)
		if (mathTerm.length() > 1 &&
				mathTerm.startsWith("$") && mathTerm.endsWith("$") &&
				!mathTerm.startsWith("$$") && !mathTerm.endsWith("$$")) {
			mathTerm = mathTerm.substring(1, mathTerm.length() - 1);
		}
		return mathTerm;
	}
}
