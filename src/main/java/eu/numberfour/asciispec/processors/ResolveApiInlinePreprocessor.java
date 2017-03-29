/**
 * Copyright (c) 2016 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.asciispec.processors;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;


/**
 * This {@link IncludeProcessor} evaluates all include macros in the document
 * which start with <code>{api}</code>.
 */
public class ResolveApiInlinePreprocessor extends MacroPreprocessor<String> implements SourceLinkMixin {
	private static final String API_INCLUDE = "apiInclude";
	private static final String GEN_ADOC_DIR_VAR = "gen_adoc_dir";

	/** File name of the index file */
	public static final String INDEX_FILE_NAME = "index.idx";

	/** source link pattern. */
	public static final Pattern API_INCLUDE_PATTERN = Pattern
			.compile("include:\\s*\\{\\s*api\\s*\\}\\s*\\+*(?<PQN>.*?)\\+*\\s*\\[(?<ATTRS>.*)\\]");
	private static final Pattern GEN_ADOC_VAR_PATTERN = Pattern.compile(":" + GEN_ADOC_DIR_VAR + ":\\s*(?<GENADOC>.*)");


	private final SourceLinkMixinState state = new SourceLinkMixinState();
	private File indexFile;

	@Override
	protected boolean init(Document document) {
		super.registerPattern(GEN_ADOC_DIR_VAR, GEN_ADOC_VAR_PATTERN);
		super.registerPattern(API_INCLUDE, API_INCLUDE_PATTERN);
		return true;
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		String newline = fullMatch;
		switch (key) {
		case GEN_ADOC_DIR_VAR:
			try {
				String genadocDirname = matcher.group("GENADOC");
				setIndexFile(genadocDirname);
			} catch (Exception e) {
				String message = e.getMessage() + " Check variable '" + GEN_ADOC_DIR_VAR + "'";
				newline += message;
				error(document, e.getMessage());
			}
			break;
		case API_INCLUDE:
			try {
				checkConfig();
				ensureDatabase();
				newline = processSourceLink(document, matcher);
			} catch (Exception e) {
				error(document, e.getMessage());
			}
			break;
		}
		return newline;
	}

	private void checkConfig() {
		if (getIndexFile() == null)
			throw new IllegalArgumentException("Missing config variable '" + GEN_ADOC_DIR_VAR + "'.");
	}

	private String processSourceLink(Document document, Matcher matcher) {
		String srclnk = matcher.group();
		String pqn = matcher.group("PQN");
		String attrs = matcher.group("ATTRS");

		IndexEntryInfoResult ieir = getIndexEntryInfo(document, srclnk, pqn);
		String repoName = "";
		String url = ieir.iei.adocPath;
		Path modulePath = getGendirPath().resolve(url);
		int startLine = ieir.iei.offsetStart;
		int endLine = ieir.iei.offsetEnd;
		String completePQN = ieir.completePQN;
		String errMsg = ieir.errorMsg;

		if (url == null)
			url = "";

		String apiText = "";

		return apiText;
	}

	@Override
	public SourceLinkMixinState getState() {
		return state;
	}

	@Override
	public String getIndexFileName() {
		return INDEX_FILE_NAME;
	}

}
