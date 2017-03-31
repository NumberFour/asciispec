package eu.numberfour.asciispec.processors;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.InlineMacroProcessor;

import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssuePrinter;

/**
 * This preprocessor provides a register method to listen for specific patterns. Pattern matching happens in the order
 * of pattern registration. When a pattern is found, a key and a matcher will be returned using the callback method
 * {@link #processMatch(Document, Object, Matcher)}. It provides similar functionality like the
 * {@link InlineMacroProcessor}, but also knows about the current file and line. The {@link MacroPreprocessor} supports
 * more than one occurrences of a given pattern e.g. a single line can contain multiple math expressions in different
 * syntaxes. The type argument T sets the type of the key for all patterns.
 */
abstract public class MacroPreprocessor<T> extends FileAwarePreprocessor implements ErrorAndWarningsMixin {

	private final LinkedHashMap<T, Pattern> patterns = new LinkedHashMap<>();

	/**
	 * Default line delimiter. This is used in case {@link #processMatch(Document, Object, Matcher)} returns multiple
	 * lines i.e. one line input results in multiple lines separated by this delimiter.
	 */
	protected final String LINE_DELIMITER = "\n";

	/**
	 * Default issue handler. Can be called directly as an alternative to the {@link #error(Document, String)} and
	 * {@link #warn(Document, String)}.
	 */
	protected final IssueAcceptor issueAcceptor = new IssuePrinter();

	/**
	 * Processes each macro match in a given line of the document. Comment regions are omitted.
	 *
	 * @param document
	 *            the document object the processor is currently working on
	 * @param key
	 *            the key that the matching pattern was registered with within {@link #init(Document)}
	 * @param matcher
	 *            the matcher for a given macro occurrence in the current line
	 * @return the line with the processed matching macros
	 */
	abstract protected String processMatch(Document document, T key, Matcher matcher);

	/**
	 * Registers a pattern and its key. Invoke this method in the {@link #init(Document)} method. You can register more
	 * than one pattern. The patterns are matched in the order they were registered.
	 */
	protected void registerPattern(T key, Pattern pattern) {
		patterns.put(key, pattern);
	}

	@Override
	final public List<String> processLine(Document document, String line) {
		String workingLine = line;
		for (Map.Entry<T, Pattern> entry : patterns.entrySet()) {
			StringBuilder builder = new StringBuilder();
			int lastEnd = 0;
			Matcher matcher = entry.getValue().matcher(workingLine);
			while (matcher.find()) {
				int startIdx = matcher.start();
				int endIdx = matcher.end();

				String replacement = null;
				try {
					replacement = processMatch(document, entry.getKey(), matcher);
				} catch (Exception e) {
					replacement = error(document, e.getMessage());
				}

				builder.append(workingLine.substring(lastEnd, startIdx));
				builder.append(replacement);
				lastEnd = endIdx;
			}
			String rest = workingLine.substring(lastEnd);
			builder.append(rest);
			workingLine = builder.toString();
		}

		return new LinkedList<>(Arrays.asList(workingLine.split(LINE_DELIMITER)));
	}

	@Override
	public IssueAcceptor getIssueAcceptor() {
		return issueAcceptor;
	}

	/*
	 * The specification of the origin of the error method is necessary, since
	 * other mixin interfaces reuse this method.
	 */
	@Override
	public String error(Document document, String consoleMsg, String inlineMsg) {
		return ErrorAndWarningsMixin.super.error(document, consoleMsg, inlineMsg);
	}

}
