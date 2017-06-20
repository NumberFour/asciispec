package eu.numberfour.asciispec.processors;

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

	private static final String REPOLNK_KEY = "repolnk";
	/** supports nested inline macros up to three levels */
	private static final Pattern REPOLNK_PATTERN = Pattern.compile(REPOLNK_KEY
			+ ":(?<REPO>[^\\[\\]:]*):((?<BRANCH>[^\\[\\]:]*):)?(?<FILE>[^\\[\\]]*)(\\[(?<ATTRS>([^\\[\\]]*|\\[([^\\[\\]]*|\\[[^\\[\\]]*\\])*\\])*)\\])");

	private static final String CONFIG_KEY = REPOLNK_KEY + "_def_";
	private static final Pattern CONFIG_PATTERN = Pattern
			.compile(":" + CONFIG_KEY + "(?<NAME>[^:]*):\\s*(?<URL>[^;]*)\\s*;\\s*(?<ICON>.*)");

	private static final String VERIFY_CONFIG_KEY = CONFIG_KEY + "loose";
	private static final Pattern VERIFY_CONFIG_PATTERN = Pattern.compile(":" + CONFIG_KEY + "(?<NAME>[^:]*):\\s*.*");

	final private Map<String, RepolnkConfig> repolnkConfigs = new HashMap<>();


	@Override
	public void init(Document document) {
		registerPattern(CONFIG_KEY, CONFIG_PATTERN);
		registerPattern(VERIFY_CONFIG_KEY, VERIFY_CONFIG_PATTERN);
		registerPattern(REPOLNK_KEY, REPOLNK_PATTERN);
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
				return error(document, "Invalid repolnk configuration: " + fullMatch);
			}
			return fullMatch;
		case REPOLNK_KEY:

			try {
				return checkAndCreateLink(document, matcher);
			} catch (IllegalArgumentException e) {
				return error(document, e.getMessage());
			} catch (Exception e) {
				return error(document, "Could not parse repolnk link: " + fullMatch);
			}
		}
		return fullMatch;
	}

	private String checkAndCreateLink(Document document, Matcher matcher) throws ParseException {
		String repo = matcher.group("REPO").trim();
		String branch = matcher.group("BRANCH");
		String file = matcher.group("FILE").trim();
		String attrStr = matcher.group("ATTRS").trim();

		if (repo == null || repo.isEmpty()) {
			throw new IllegalArgumentException("Missing repo in repolnk: '" + matcher.group(0) + "'");
		}
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Missing file in repolnk: '" + matcher.group(0) + "'");
		}
		if (!repolnkConfigs.containsKey(repo)) {
			throw new IllegalArgumentException("Missing repolnk configuration for repo: '" + repo + "'");
		}
		if (branch == null) {
			branch = "master";
		} else {
			branch = branch.trim();
		}

		RepolnkConfig config = repolnkConfigs.get(repo);
		return createLink(config, repo, branch, file, attrStr);
	}

	private String createLink(RepolnkConfig config, String repo, String branch, String file, String attrStr)
			throws ParseException {

		Map<String, Object> rawAttrMap = new HashMap<>();
		rawAttrMap.put("text", attrStr);
		String linkText = AdocUtils.getRawAttributeAsString(rawAttrMap, "title", 0, file);

		Map<String, String> templateVars = new HashMap<>();
		templateVars.put("BRANCH", branch);
		templateVars.put("FILE", file);
		String url = AdocUtils.transformVariables(config.urlTemplate, templateVars);

		String imgLink = "";
		if (config.iconFile != null)
			imgLink = AdocUtils.createImageWithLink(config.iconFile, url, url);
		String txtLink = AdocUtils.createLinkWithTitle(url, linkText, url);

		return imgLink + txtLink;
	}

	private void addConfig(Matcher matcher) {
		String name = matcher.group("NAME").trim();
		String urlTemplate = matcher.group("URL").trim();

		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Invalid repolnk configuration: 'NAME'");
		if (urlTemplate == null || urlTemplate.isEmpty())
			throw new IllegalArgumentException("Invalid repolnk configuration: 'URL'");
		if (!urlTemplate.contains("{BRANCH}"))
			throw new IllegalArgumentException("Invalid repolnk configuration in URL: BRANCH placeholder missing");
		if (!urlTemplate.contains("{FILE}"))
			throw new IllegalArgumentException("Invalid repolnk configuration in URL: FILE placeholder missing");

		String iconFile = matcher.group("ICON");
		if (iconFile != null)
			iconFile = iconFile.trim();
		if (iconFile.isEmpty())
			iconFile = null;
		RepolnkConfig config = new RepolnkConfig(name, urlTemplate, iconFile);
		repolnkConfigs.put(config.name, config);
	}

	private boolean verifyConfiguration(Matcher matcher) {
		String name = matcher.group("NAME");
		boolean alreadyConfigured = repolnkConfigs.containsKey(name);
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
