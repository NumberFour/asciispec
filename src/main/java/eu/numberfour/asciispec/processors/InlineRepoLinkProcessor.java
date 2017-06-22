package eu.numberfour.asciispec.processors;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.ParseException;

/**
 */
public class InlineRepoLinkProcessor extends MacroPreprocessor<String> {

	private static final String REPO_KEY = "repo";
	/** supports nested inline macros up to three levels */
	private static final Pattern REPO_PATTERN = Pattern.compile(REPO_KEY
			+ ":(?<REPO>[^\\[\\]:]*):((?<BRANCH>[^\\[\\]:]*):)?(?<FILE>[^\\[\\]]*)(\\[(?<ATTRS>([^\\[\\]]*|\\[([^\\[\\]]*|\\[[^\\[\\]]*\\])*\\])*)\\])");

	private static final String CONFIG_KEY = REPO_KEY + "_def_";
	private static final Pattern CONFIG_PATTERN = Pattern
			.compile(":" + CONFIG_KEY + "(?<NAME>[^:]*):\\s*(?<URL>[^;]*)\\s*;\\s*(?<ICON>.*)");

	private static final String VERIFY_CONFIG_KEY = CONFIG_KEY + "loose";
	private static final Pattern VERIFY_CONFIG_PATTERN = Pattern.compile(":" + CONFIG_KEY + "(?<NAME>[^:]*):\\s*.*");

	final private Map<String, RepolnkConfig> repoConfigs = new HashMap<>();


	@Override
	public void init(Document document) {
		registerPattern(CONFIG_KEY, CONFIG_PATTERN);
		registerPattern(VERIFY_CONFIG_KEY, VERIFY_CONFIG_PATTERN);
		registerPattern(REPO_KEY, REPO_PATTERN);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		switch (key) {
		case CONFIG_KEY:
			try {
				addConfig(matcher);
			} catch (IllegalArgumentException e) {
				return error(document, e.getMessage());
			}
			return fullMatch;
		case VERIFY_CONFIG_KEY:
			boolean isConfigured = verifyConfiguration(matcher);
			if (!isConfigured) {
				return error(document, "Invalid repo configuration: " + fullMatch);
			}
			return fullMatch;
		case REPO_KEY:

			try {
				return checkAndCreateLink(document, matcher);
			} catch (IllegalArgumentException e) {
				return error(document, e.getMessage());
			} catch (Exception e) {
				return error(document, "Could not parse repo macro: " + fullMatch);
			}
		}
		return fullMatch;
	}

	private String checkAndCreateLink(Document document, Matcher matcher) throws ParseException {
		String repo = matcher.group("REPO").trim();
		String branch = matcher.group("BRANCH");
		String file = matcher.group("FILE").trim();
		String attrStr = matcher.group("ATTRS").trim();

		if (isNullOrEmpty(repo)) {
			throw new IllegalArgumentException("Missing attribute 'repo' in repo macro: '" + matcher.group(0) + "'");
		}
		if (isNullOrEmpty(file)) {
			throw new IllegalArgumentException("Missing attribute 'file' in repo macro: '" + matcher.group(0) + "'");
		}
		if (!repoConfigs.containsKey(repo)) {
			throw new IllegalArgumentException("Missing repo_def configuration for repository name: '" + repo + "'");
		}
		if (branch == null) {
			branch = "master";
		} else {
			branch = branch.trim();
		}

		RepolnkConfig config = repoConfigs.get(repo);
		return createLink(config, repo, branch, file, attrStr);
	}

	private String createLink(RepolnkConfig config, String repo, String branch, String file, String attrStr)
			throws ParseException {

		Map<String, Object> rawAttrMap = new HashMap<>();
		rawAttrMap.put("text", attrStr);
		String linkText = AdocUtils.getRawAttributeAsString(rawAttrMap, "title", 0, file);
		// the following line removes all occurrences of []-blocks
		// since they are not supported in the image macro
		String linkTitleSimple = linkText.replaceAll("((?<=[^\\\\])\\[|^\\[)((.*[^\\\\])\\]|\\])", "");

		Map<String, String> templateVars = new HashMap<>();
		templateVars.put("BRANCH", branch);
		templateVars.put("FILE", file);
		String url = AdocUtils.transformVariables(config.urlTemplate, templateVars);

		String imgLink = "";
		if (config.iconFile != null)
			imgLink = AdocUtils.createImageWithLink(config.iconFile, linkTitleSimple, url);
		String txtLink = AdocUtils.createLinkWithTitle(url, linkTitleSimple, url);

		return imgLink + txtLink;
	}

	private void addConfig(Matcher matcher) {
		String name = matcher.group("NAME").trim();
		String urlTemplate = matcher.group("URL").trim();

		if (isNullOrEmpty(name))
			throw new IllegalArgumentException("Invalid repo_def configuration: 'NAME' missing");
		if (urlTemplate == null || urlTemplate.isEmpty())
			throw new IllegalArgumentException("Invalid repo_def configuration: 'URL' missing");
		if (!urlTemplate.contains("{BRANCH}"))
			throw new IllegalArgumentException("Invalid repo_def configuration in URL: BRANCH placeholder missing");
		if (!urlTemplate.contains("{FILE}"))
			throw new IllegalArgumentException("Invalid repo_def configuration in URL: FILE placeholder missing");

		String iconFile = matcher.group("ICON");
		if (iconFile != null)
			iconFile = iconFile.trim();
		if (iconFile.isEmpty())
			iconFile = null;
		RepolnkConfig config = new RepolnkConfig(name, urlTemplate, iconFile);
		repoConfigs.put(config.name, config);
	}

	private boolean verifyConfiguration(Matcher matcher) {
		String name = matcher.group("NAME");
		boolean alreadyConfigured = repoConfigs.containsKey(name);
		return alreadyConfigured;
	}

	static class RepolnkConfig {
		final String name;
		final String urlTemplate;
		final String iconFile;

		RepolnkConfig(String name, String urlTemplate, String iconFile) {
			this.name = name;
			this.urlTemplate = urlTemplate;
			this.iconFile = iconFile;
		}
	}
}
