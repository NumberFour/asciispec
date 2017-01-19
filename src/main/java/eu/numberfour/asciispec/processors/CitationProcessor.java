package eu.numberfour.asciispec.processors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.citation.BibTexParser;
import eu.numberfour.asciispec.citation.BibliographyDatabase;
import eu.numberfour.asciispec.citation.BibliographyEntry;
import eu.numberfour.asciispec.citation.CitationParser;
import eu.numberfour.asciispec.citation.CitationReference;
import eu.numberfour.asciispec.citation.IdentityBibTexTagProcessor;
import eu.numberfour.asciispec.issue.IssueCollector;

/**
 * A preprocessor that processes citations.
 *
 * <p>
 * Citations are denoted inline using the <code>cite:[]</code> macro, and the bibliography is placed in a document using
 * the <code>bibliography::[]</code> macro. For a description of the syntax for citations, see {@link CitationParser}.
 * </p>
 *
 * <p>
 * The macros are processed as follows.
 * <ol>
 * <li>The bibliography database is located and loaded using a two-part strategy:
 * <ul>
 * <li>If the document contains the <code>:bib-file:</code> attribute, its value is interpreted as a relative path. This
 * path is resolved against the path to the folder containing the document being processed.
 * <li>If the document does not contain a <code>:bib-file:</code> attribute or the file could not be found, the
 * processor attempts to locate a file with the extension <code>.bib</code> by recursively searching for a file in the
 * folder that contains the document being processed.</li>
 * </ul>
 * </li>
 * <li>The <code>cite:[]</code> macros are processed and replaced with citation strings. The cited bibliography entries
 * are stored for later when the bibliography macro is processed.</li>
 * <li>The <code>bibliography::[]</code> macro is processed and replaced with the actual bibliography. Thereby only
 * those entries are printed which have actually been cited in the document.</li>
 * </ol>
 * </p>
 *
 * <p>
 * Both macros are implemented using a preprocessor rather than an inline macro processor (for <code>cite:[]</code>) and
 * a block macro processor (for <code>bibliography::[]</code>) for two reasons. First, it is necessary to store the
 * bibliography and the citations at a location that is accessible to both macros. Had the two macros been implemented
 * as separate processors, this would have required the use of a singleton. Second, and more importantly, the
 * <code>cite:[]</code> macros have to be processed before the <code>bibliography::[]</code> macro in order to store the
 * citations that the bibliography macro uses to retrieve the bibliography entries that have actually been cited.
 * asciidoctor however invokes inline macros after it invokes block macro processors, so the bibliography macro be
 * invoked before the actual citations have been processed, outputting nothing. Therefore, the both macros must be
 * processed using a preprocessor.
 * </p>
 *
 * <p>
 * Note that, contrary to intuition, both the cite:[] and the bibliography::[] macros are processed inside source
 * blocks. This allows the user to put citations inside source code listings. The macros are not processed however if
 * they are placed within pass blocks (delimited by <code>++++</code>), inline pass phrases (delimited by
 * <code>+++</code>) or an inline pass macro (<code>pass:[cite:[...]]</code>).
 * </p>
 */
public class CitationProcessor extends MacroPreprocessor<String> {

	private static final String BIB_FILE_KEY = "bibFile";
	private static final Pattern BIB_FILE_PATTERN = Pattern.compile(":bib-file:\\s*([^\\s]+)");

	private static final String CITE_KEY = "cite";
	private static final Pattern CITE_PATTERN = Pattern.compile("cite:\\[([^\\]]*)\\]");

	private static final String BIBLIOGRAPHY_KEY = "bibliography";
	private static final Pattern BIBLIOGRAPHY_PATTERN = Pattern.compile("\\s*bibliography::\\[([^\\]]*)\\]\\s*");

	/**
	 * The state of the database.
	 */
	private static enum DatabaseState {
		/**
		 * Initial state - loading hasn't been attempted yet.
		 */
		INITIAL,
		/**
		 * The database was loaded successfully.
		 */
		LOADED,
		/**
		 * Loading has failed and will not be attempted again.
		 */
		FAILED
	}

	private Path documentBasePath;
	private Path configuredBibTexFile;

	private DatabaseState databaseState = DatabaseState.INITIAL;
	private BibliographyDatabase database;

	LinkedHashSet<CitationReference> citations = new LinkedHashSet<>();

	@Override
	protected boolean init(Document document) {
		registerPattern(BIB_FILE_KEY, BIB_FILE_PATTERN);
		registerPattern(CITE_KEY, CITE_PATTERN);
		registerPattern(BIBLIOGRAPHY_KEY, BIBLIOGRAPHY_PATTERN);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case BIB_FILE_KEY:
			documentBasePath = AdocUtils.getDocumentBasePath(document);
			configuredBibTexFile = Paths.get(matcher.group(1));
			return fullMatch;
		case CITE_KEY:
			return processCitation(document, matcher);
		case BIBLIOGRAPHY_KEY:
			return String.join(LINE_DELIMITER, formatBibliography(document));
		}
		return fullMatch;
	}

	private String processCitation(Document document, Matcher matcher) {
		String fullMatch = matcher.group();
		if (!ensureDatabaseLoaded(document))
			return fullMatch;

		String attributes = matcher.group(1);

		try {
			StringBuilder result = new StringBuilder();
			result.append("+[+"); // need to escape, since these confuse AsciiDoctor, see AS105

			List<CitationReference> references = CitationParser.parse(attributes);
			Iterator<CitationReference> referenceIt = references.iterator();
			while (referenceIt.hasNext()) {
				CitationReference reference = referenceIt.next();
				formatReference(reference, result);
				if (referenceIt.hasNext())
					result.append("; ");
				citations.add(reference);
			}
			result.append("+]+"); // need to escape, since these confuse AsciiDoctor, see AS105
			return result.toString();
		} catch (ParseException e) {
			error(document, "Could not parse citation references: " + e.getMessage());
			return fullMatch;
		}
	}

	private void formatReference(CitationReference reference, StringBuilder result) {
		result.append("<<").append(reference.getCiteKey()).append(",").append(reference.getCiteKey());
		formatPages(reference, result);
		result.append(">>");
	}

	private void formatPages(CitationReference reference, StringBuilder result) {
		final List<String> pages = reference.getPages();
		if (!pages.isEmpty()) {
			result.append("(");
			Iterator<String> pageIt = pages.iterator();
			do {
				result.append("p.").append(pageIt.next());
				if (pageIt.hasNext())
					result.append(", ");
			} while (pageIt.hasNext());
			result.append(")");
		}
	}

	private LinkedList<String> formatBibliography(ContentNode parentNode) {
		IssueCollector issues = new IssueCollector(issueAcceptor);

		LinkedList<String> result = new LinkedList<>();
		if (!ensureDatabaseLoaded(parentNode.getDocument()))
			return result;

		result.add("[role=\"bibliography\"]");
		result.add("--");
		for (CitationReference citation : citations) {
			result.add(formatBibliographyEntry(parentNode, citation, issues));
			result.add("");
		}
		result.add("--");
		issues.appendTo(result);
		return result;
	}

	private String formatBibliographyEntry(ContentNode parentNode, CitationReference citation, IssueCollector issues) {
		BibliographyEntry entry = database.findByCiteKey(citation.getCiteKey());
		if (entry == null) {
			String message = "Unknown reference: " + citation.toString();
			issues.warn(parentNode, message);
			return message;
		}

		StringBuilder result = new StringBuilder();
		result.append("[[").append(citation.getCiteKey()).append("]]");
		appendIfPresent(entry, "author", "", ". ", result);
		appendIfPresent(entry, "year", "(", "). ", result);
		appendWithDefault(entry, "title", "Unknown Title", "_", "_. ", result);
		appendIfPresent(entry, "organization", "", ". ", result);
		appendIfPresent(entry, "url", "Retrieved from <", ">", result);

		return result.toString();
	}

	private void appendIfPresent(BibliographyEntry entry, String tagName, String prefix, String suffix,
			StringBuilder result) {
		String value = entry.getTag(tagName, null);
		if (value != null)
			result.append(prefix).append(value).append(suffix);
	}

	private void appendWithDefault(BibliographyEntry entry, String tagName, String defaultValue, String prefix,
			String suffix,
			StringBuilder result) {
		result.append(prefix).append(entry.getTag(tagName, defaultValue)).append(suffix);
	}

	private boolean ensureDatabaseLoaded(Document document) {
		switch (databaseState) {
		case INITIAL:
			loadDatabase(document);
			break;
		default:
			break;
		}

		return databaseState == DatabaseState.LOADED;
	}

	/**
	 * Loads the bibtex database using the information stored in the following properties:
	 *
	 * <ul>
	 * <li>{@link #documentBasePath} - the path to the folder that contains the document</li>
	 * <li>{@link #configuredBibTexFile} - value of the <code>:bib-file:</code> document attribute</li>
	 * </ul>
	 */
	private void loadDatabase(Document document) {
		Path bibTexFilePath = getActualPath(document, configuredBibTexFile);
		if (bibTexFilePath == null) {
			issueAcceptor.error(document, "Could not find a BibTeX file");
			databaseState = DatabaseState.FAILED;
		} else {
			doLoadBibTexDatabase(document, bibTexFilePath);
		}
	}

	private Path getActualPath(Document document, Path filePath) {
		if (filePath != null) {
			Path absPath = null;
			if (filePath.isAbsolute()) {
				absPath = filePath;
			} else {
				if (documentBasePath != null)
					absPath = documentBasePath.resolve(filePath);
				else
					absPath = filePath;
			}

			final File file = absPath.toFile();
			if (file.exists() && file.isFile())
				return absPath;
		}

		return findBibTexFile(document);
	}

	private Path findBibTexFile(Document document) {
		try {
			final Path searchPath = documentBasePath;
			if (searchPath == null)
				return null;

			BibTexFileFinder finder = new BibTexFileFinder();
			Files.walkFileTree(searchPath, finder);
			return finder.getBestMatch();
		} catch (IOException e) {
			issueAcceptor.error(document, "Error while searching for BibTeX file: " + e.getMessage());
			return null;
		}
	}

	private static class BibTexFileFinder extends SimpleFileVisitor<Path> {
		private static class Match {
			final int depth;
			final Path path;

			public Match(int depth, Path path) {
				this.depth = depth;
				this.path = path;
			}

			public Match() {
				this.depth = Integer.MAX_VALUE;
				this.path = null;
			}
		}

		private int currentDepth = 0;
		private Match bestMatch = new Match();

		/**
		 * Returns the best match. Among all visited files, this is the first file with the extension <code>.bib</code>
		 * that was visited having a minimal nesting depth in the directory tree.
		 *
		 * @return the best matching file or <code>null</code> if no file with the extension <code>.bib</code> was
		 *         found.
		 */
		public Path getBestMatch() {
			return bestMatch.path;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			currentDepth++;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (bestMatch.depth > currentDepth && file.getFileName().toString().toLowerCase().endsWith(".bib"))
				bestMatch = new Match(currentDepth, file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			currentDepth--;
			return FileVisitResult.CONTINUE;
		}
	}

	private void doLoadBibTexDatabase(Document document, Path path) {
		final Path absPath = path.toAbsolutePath();
		try {
			final List<BibliographyEntry> entries = BibTexParser.parse(absPath, StandardCharsets.UTF_8,
					new IdentityBibTexTagProcessor());
			database = new BibliographyDatabase(entries);
			databaseState = DatabaseState.LOADED;
		} catch (NoSuchFileException e) {
			issueAcceptor.error(document,
					"Failed to load citation database: File '" + absPath + "' not found: " + e.getMessage());
			databaseState = DatabaseState.FAILED;
		} catch (IOException e) {
			issueAcceptor.error(document,
					"Failed to load citation database: File '" + absPath + "' could not be opened: " + e.getMessage());
			databaseState = DatabaseState.FAILED;
		} catch (ParseException e) {
			issueAcceptor.error(document,
					"Failed to load citation database: File '" + absPath + "' could not be parsed: " + e.getMessage());
			databaseState = DatabaseState.FAILED;
		}
	}
}
