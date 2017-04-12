package eu.numberfour.asciispec.processors;

import static eu.numberfour.asciispec.AdocUtils.transformVariable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.sourceindex.IndexEntryInfo;

/**
 * A preprocessor that processes source links to the code management system
 * (e.g. GitHib).<br/>
 * <br/>
 * Source links have the syntax <code>srclnk::&ltPQN>[&ltLabel>]</code> where
 * PQN is a partial qualified name. The source link points to a source code line
 * and file in the code management system on e.g. GitHub. The label of the
 * source link is usually the PQN. However, the label can be adjusted using the
 * optional Label parameter.<br/>
 * <br/>
 * To use the preprocessor, two asciispec variables have to be set. First, the
 * directory of the generated documentation has to be set using the variable
 * <code>:gen_adoc:</code>. This directory must point to the generated
 * 'gen_adoc' directory. Second, the code management systems have to be set
 * using the variable <code>:srclin_repo_def_&lt;ID&gt;:</code>.<br/>
 * <br/>
 * For more information about the PQN syntax and asciispec variables, please
 * refer to the asciispec reference.
 */
public class SourceLinkPreprocessor extends MacroPreprocessor<String> implements SourceIndexMixin {
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
	public static final Pattern SRC_LINK_PATTERN = Pattern.compile(SRCLNK
			+ ":(?<A>\\+*)(?<PQN>.*?)(\\k<A>)\\[(?<MARKUP1>[`_]*)(?<B>\\+*)(?<LABEL>.*?)(\\k<B>)(?<MARKUP2>[`_]*)\\]");
	private static final Pattern REPO_CONFIG_VAR_PATTERN = Pattern.compile(
			":" + REPOS_CONFIG_VAR + ":\\s*(?<NAME>.*?)\\s*;\\s*(?<DESCR>.*?)\\s*;\\s*(?<HTML>https?:\\/\\/[\\S]+)");
	private static final Pattern GEN_ADOC_VAR_PATTERN = Pattern.compile(":" + GEN_ADOC_DIR_VAR + ":\\s*(?<GENADOC>.*)");

	private final Map<String, RepositoryConfig> repoConfigs = new HashMap<>();
	private final SourceIndexMixinState state = new SourceIndexMixinState();

	@Override
	public SourceIndexMixinState getState() {
		return state;
	}

	@Override
	public void init(Document document) {
		super.registerPattern(GEN_ADOC_DIR_VAR, GEN_ADOC_VAR_PATTERN);
		super.registerPattern(REPOS_CONFIG_VAR, REPO_CONFIG_VAR_PATTERN);
		super.registerPattern(SRCLNK, SRC_LINK_PATTERN);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		String newline = fullMatch;

		switch (key) {
		case GEN_ADOC_DIR_VAR:
			newline = setGenDir(document, matcher, newline);
			break;
		case REPOS_CONFIG_VAR:
			newline = addRepoConfig(document, fullMatch, matcher);
			break;
		case SRCLNK:
			newline = processSrclnk(document, matcher, newline);
			break;
		}
		return newline;
	}

	private String setGenDir(Document document, Matcher matcher, String newline) {
		try {
			String genadocDirname = matcher.group("GENADOC");
			Path genadocPath = Paths.get(genadocDirname);
			File genadocFile = getAbsoluteFileFromBaseDirectory(genadocPath);
			setIndexFile(genadocFile);
		} catch (Exception e) {
			String message = e.getMessage() + ". Check variable '" + GEN_ADOC_DIR_VAR + "'";
			newline += "\n\n" + error(document, message) + "\n\n";
		}
		return newline;
	}

	private String addRepoConfig(Document document, String line, Matcher cgfReposVar) {
		String newLine = line;
		try {
			String name = cgfReposVar.group("NAME");
			String descr = cgfReposVar.group("DESCR");
			String html = cgfReposVar.group("HTML");
			RepositoryConfig repoCfg = new RepositoryConfig(name, descr, html);
			repoConfigs.put(repoCfg.name, repoCfg);
		} catch (Exception e) {
			newLine += "\n\n"
					+ error(document, "Error when parsing repository config. Check variable: " + REPOS_CONFIG_VAR);
		}
		return newLine;
	}

	private String processSrclnk(Document document, Matcher matcher, String newline) {
		try {
			checkConfig();
			ensureDatabase();
			newline = processSourceLink(document, matcher);
		} catch (Exception e) {
			newline += "\n\n" + error(document, e.getMessage());
		}
		return newline;
	}

	private void checkConfig() {
		if (getIndexFile() == null)
			throw new IllegalArgumentException("Missing config variable '" + GEN_ADOC_DIR_VAR + "'.");

		if (repoConfigs.isEmpty())
			throw new IllegalArgumentException("Missing config variable '" + REPOS_CONFIG_VAR + "'.");
	}

	private String processSourceLink(Document document, Matcher matcher) {
		String srclnk = matcher.group();
		String pqn = matcher.group("PQN");
		String label = matcher.group("LABEL");
		String markup1 = matcher.group("MARKUP1");
		String markup2 = matcher.group("MARKUP2");

		IndexEntryInfoResult ieir = getIndexEntryInfo(document, srclnk, pqn);

		String repoName = "";
		String url = ieir.url;
		String completePQN = ieir.completePQN;
		String errMsg = ieir.errorMsg;
		if (ieir.iei != null) {
			try {
				IndexEntryInfo iei = ieir.iei;
				repoName = iei.repository;
				completePQN = iei.toPQN();
				RepositoryConfig repoConfig = repoConfigs.get(repoName);
				if (repoConfig == null) {
					throw new IllegalArgumentException("No repository found for source link. Add config variable '"
							+ REPOS_CONFIG_VAR + "' for repository '" + repoName + "'.");
				}
				url = getCMSUrl(repoConfig, iei.getRepoRelativeURL(), iei.sourceLine);

			} catch (IllegalArgumentException e) {
				errMsg = error(document, "Missing srclnk repository configuration found for: '" + repoName + "'.",
						"Missing config for repository '" + repoName + "'");
			}
		}

		String result;
		if (errMsg != null) {
			result = srclnk + "\n" + errMsg;
		} else {
			url = (url == null) ? "" : url;
			result = "link:++" + url + "++[" + markup1 + "++" + label + "++" + markup2 + ", title=\"" + completePQN
					+ "\", window=\"_blank\"]";
		}

		return result;
	}

	private String getCMSUrl(RepositoryConfig repoConfig, String relUrl, int lineNumber) {
		String transf = transformVariable(repoConfig.urlPattern, CMS_PATH, relUrl);
		transf = transformVariable(transf, LINE_NO, String.valueOf(lineNumber));
		return transf;
	}

}
