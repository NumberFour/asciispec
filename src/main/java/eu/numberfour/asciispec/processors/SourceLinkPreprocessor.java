package eu.numberfour.asciispec.processors;

import static eu.numberfour.asciispec.AdocUtils.transformVariable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;
import eu.numberfour.asciispec.sourceindex.AmbiguousPQNExcpetion;
import eu.numberfour.asciispec.sourceindex.IndexEntryInfo;
import eu.numberfour.asciispec.sourceindex.IndexFileParser;
import eu.numberfour.asciispec.sourceindex.NotInSourceIndexExcpetion;
import eu.numberfour.asciispec.sourceindex.PQNParser;
import eu.numberfour.asciispec.sourceindex.SourceIndexDatabase;

/**
 * A preprocessor that processes source links to the code management system (e.g. GitHib).<br/>
 * <br/>
 * Source links have the syntax <code>srclnk::&ltPQN>[&ltLabel>]</code> where PQN is a partial qualified name. The
 * source link points to a source code line and file in the code management system on e.g. GitHub. The label of the
 * source link is usually the PQN. However, the label can be adjusted using the optional Label parameter.<br/>
 * <br/>
 * To use the preprocessor, two asciispec variables have to be set. First, the directory of the generated documentation
 * has to be set using the variable <code>:gen_adoc:</code>. This directory must point to the generated 'gen_adoc'
 * directory. Second, the code management systems have to be set using the variable
 * <code>:srclin_repo_def_&lt;ID&gt;:</code>.<br/>
 * <br/>
 * For more information about the PQN syntax and asciispec variables, please refer to the asciispec reference.
 */
public class SourceLinkPreprocessor extends MacroPreprocessor<String> {
	@SuppressWarnings("unused")
	private static class RepositoryConfig {
		public final String name;
		public final String description;
		public final String urlPattern;

		public RepositoryConfig(String name, String description, String url) {
			this.name = name;
			this.description = description;
			this.urlPattern = url;
		}
	}

	private static final String SRCLNK = "srclnk";
	private static final String GEN_ADOC_DIR_VAR = "gen_adoc_dir";
	private static final String REPOS_CONFIG_VAR = "srclnk_repo_def";
	private static final String CMS_PATH = "CMS_PATH";
	private static final String LINE_NO = "LINE_NO";

	/** File name of the index file */
	public static final String INDEX_FILE_NAME = "index.idx";

	/** source link pattern. */
	public static final Pattern SRC_LINK_PATTERN = Pattern.compile(
			SRCLNK + ":(?<A>\\+*)(?<PQN>.*?)(\\k<A>)\\[(?<MARKUP1>[`_]*)(?<B>\\+*)(?<LABEL>.*?)(\\k<B>)(?<MARKUP2>[`_]*)\\]");
	private static final Pattern REPO_CONFIG_VAR_PATTERN = Pattern.compile(
			":" + REPOS_CONFIG_VAR + ":\\s*(?<NAME>.*?)\\s*;\\s*(?<DESCR>.*?)\\s*;\\s*(?<HTML>https?:\\/\\/[\\S]+)");
	private static final Pattern GEN_ADOC_VAR_PATTERN = Pattern.compile(":" + GEN_ADOC_DIR_VAR + ":\\s*(?<GENADOC>.*)");

	// TODO: make this configurable
	private final Map<String, RepositoryConfig> repoConfigs = new HashMap<>();
	private File indexFile;
	private SourceIndexDatabase database;
	private boolean configuring = true;

	@Override
	protected boolean init(Document document) {
		super.registerPattern(GEN_ADOC_DIR_VAR, GEN_ADOC_VAR_PATTERN);
		super.registerPattern(REPOS_CONFIG_VAR, REPO_CONFIG_VAR_PATTERN);
		super.registerPattern(SRCLNK, SRC_LINK_PATTERN);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		String newline = fullMatch;
		switch (key) {
		case GEN_ADOC_DIR_VAR:
			try {
				newline = setIndexFile(fullMatch, matcher);
			} catch (Exception e) {
				String message = e.getMessage() + " Check variable '" + GEN_ADOC_DIR_VAR + "'";
				newline += message;
				issueAcceptor.error(document, message, getCurrentFile(), getCurrentLine());
			}
			break;
		case REPOS_CONFIG_VAR:
			newline = addRepoConfig(fullMatch, matcher);
			break;
		case SRCLNK:
			try {
				checkConfig();
				ensureDatabase();
				newline = processSourceLink(document, matcher, fullMatch);
			} catch (Exception e) {
				issueAcceptor.error(document, e.getMessage(), getCurrentFile(), getCurrentLine());
			}
			break;
		}
		return newline;
	}

	private String setIndexFile(String line, Matcher cgfGenAdoc)
			throws FileNotFoundException, MultipleFileMatchesException {

		if (!configuring) {
			throw new IllegalArgumentException(
					"The configuration must not be specified after first use of the source link macro");
		}

		String genadoc = cgfGenAdoc.group("GENADOC");
		Path genadocDir = Paths.get(genadoc, INDEX_FILE_NAME);
		String indexFileName = genadocDir.toString();
		indexFile = super.searchFile(indexFileName);
		return line;
	}

	private String addRepoConfig(String line, Matcher cgfReposVar) {
		String newLine = line;
		String name = cgfReposVar.group("NAME");
		String descr = cgfReposVar.group("DESCR");
		String html = cgfReposVar.group("HTML");
		RepositoryConfig repoCfg = new RepositoryConfig(name, descr, html);
		repoConfigs.put(repoCfg.name, repoCfg);
		return newLine;
	}

	private void checkConfig() {
		if (indexFile == null)
			throw new IllegalArgumentException("Missing config variable '" + GEN_ADOC_DIR_VAR + "'.");

		if (repoConfigs.isEmpty())
			throw new IllegalArgumentException("Missing config variable '" + REPOS_CONFIG_VAR + "'.");
	}

	private String processSourceLink(Document document, Matcher matcher, String srclnk) {
		String pqn = matcher.group("PQN");
		String label = matcher.group("LABEL");
		String markup1 = matcher.group("MARKUP1");
		String markup2 = matcher.group("MARKUP2");
		List<String> pqnStack = null;

		try {
			pqnStack = PQNParser.parse(pqn);
		} catch (ParseException e) {
			issueAcceptor.error(document,
					"srclnk could not be parsed: '" + srclnk + "'.");
			label += " [Error: PQN malformed]";
		}

		String url = null;
		String repoName = "";
		String completePQN = pqn;
		try {
			IndexEntryInfo iei = database.getEntry(pqnStack);
			repoName = iei.repository;
			completePQN = iei.toPQN();
			RepositoryConfig repoConfig = repoConfigs.get(repoName);
			if (repoConfig == null) {
				throw new IllegalArgumentException(
						"No repository found for source link. Add config variable '" + REPOS_CONFIG_VAR +
								"' for repository '" + repoName + "'.");
			}
			url = getCMSUrl(repoConfig, iei.getRepoRelativeURL(), iei.sourceLine);

		} catch (AmbiguousPQNExcpetion e) {
			issueAcceptor.error(document, "srclnk PQN is ambiguous: '" + pqn + "'.");
			label += " [Error: Ambiguous PQN]";
		} catch (NotInSourceIndexExcpetion e) {
			issueAcceptor.error(document, "srclnk PQN not found: '" + pqn + "'.");
			label += " [Error: PQN not found]";
		} catch (IllegalArgumentException e) {
			issueAcceptor.error(document,
					"Missing srclnk repository configuration found for : '" + repoName + "'.");
			label += " [Error: Missing config for repository '" + repoName + "']";
		}

		if (url == null)
			url = "";

		String link = "link:++" + url + "++[" + markup1 + "++" + label + "++" + markup2 + ", title=\"" + completePQN
				+ "\", window=\"_blank\"]";

		return link;
	}

	private String getCMSUrl(RepositoryConfig repoConfig, String relUrl, int lineNumber) {
		String transf = transformVariable(repoConfig.urlPattern, CMS_PATH, relUrl);
		transf = transformVariable(transf, LINE_NO, String.valueOf(lineNumber));
		return transf;
	}

	/**
	 * The expected structure of the root directory is as follows:
	 * <ul>
	 * <li>root/
	 * <ul>
	 * <li>adoc_gen/
	 * <ul>
	 * <li>index.idx
	 * <li>...
	 * </ul>
	 * <li>html/
	 * <li>...
	 * </ul>
	 * </ul>
	 */
	private void ensureDatabase() throws IOException, ParseException {
		if (database != null)
			return;

		database = new SourceIndexDatabase();
		database = IndexFileParser.parse(indexFile.toPath(), StandardCharsets.UTF_8);
		configuring = false;
	}

}
