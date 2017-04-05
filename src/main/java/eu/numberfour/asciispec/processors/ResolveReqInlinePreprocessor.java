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

import static eu.numberfour.asciispec.processors.AttributeUtils.appendLeveloffset;
import static eu.numberfour.asciispec.processors.AttributeUtils.getLeveloffset;
import static eu.numberfour.asciispec.processors.AttributeUtils.isInSelectedLineRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.AttributeParser;
import eu.numberfour.asciispec.ParseException;

/**
 * This processor evaluates all inline {req} macros, which were previously
 * created by the {@link ResolveReqInlinePreprocessor}. The requirement ID of
 * the include is parsed and the corresponding API is extracted from the
 * generated API documentation. The attributes <code>lines</code> and
 * <code>leveloffset</code> are supported.
 */
public class ResolveReqInlinePreprocessor extends MacroPreprocessor<String> {
	private static final String REQ_INCLUDE = "reqInclude";
	private static final String GEN_ADOC_DIR_VAR = "gen_adoc_dir";

	/** source link pattern. */
	public static final Pattern REQ_INCLUDE_PATTERN = Pattern
			.compile("include:\\s*\\{\\s*req\\s*\\}\\s*\\+*(?<REQID>.*?)\\+*\\s*\\[(?<ATTRS>.*)\\]");
	private static final Pattern GEN_ADOC_VAR_PATTERN = Pattern.compile(":" + GEN_ADOC_DIR_VAR + ":\\s*(?<GENADOC>.*)");

	private Path genReqsDir;

	@Override
	public void init(Document document) {
		super.registerPattern(GEN_ADOC_DIR_VAR, GEN_ADOC_VAR_PATTERN);
		super.registerPattern(REQ_INCLUDE, REQ_INCLUDE_PATTERN);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		String fullMatch = matcher.group();
		String newline = fullMatch;

		switch (key) {
		case GEN_ADOC_DIR_VAR:
			newline = setGenDir(document, matcher, newline);
			break;
		case REQ_INCLUDE:
			newline = processReqIncl(document, matcher, newline);
			break;
		}
		return newline;
	}

	private String setGenDir(Document document, Matcher matcher, String newline) {
		try {
			String genadocDirname = matcher.group("GENADOC");
			Path genadocPath = Paths.get(genadocDirname);
			genReqsDir = genadocPath.resolve("requirements");
		} catch (Exception e) {
			String message = e.getMessage() + ". Check variable '" + GEN_ADOC_DIR_VAR + "'";
			newline += "\n\n" + error(document, message) + "\n\n";
		}
		return newline;
	}

	private String processReqIncl(Document document, Matcher matcher, String newline) {
		try {
			checkConfig();
			newline = processReqInclude(document, matcher);
		} catch (Exception e) {
			newline += "\n\n" + error(document, e.getMessage());
		}
		return newline;
	}

	private void checkConfig() {
		if (genReqsDir == null)
			throw new IllegalArgumentException("Missing config variable '" + GEN_ADOC_DIR_VAR + "'.");
	}

	private String processReqInclude(Document document, Matcher matcher) {
		String apiInclude = matcher.group();
		String reqid = matcher.group("REQID");
		String attrs = matcher.group("ATTRS");

		StringBuilder strb = new StringBuilder();

		String errMsg = null;
		Path modulePath = null;
		String fileName = reqid + ".adoc";
		try {
			modulePath = genReqsDir.resolve(fileName);
		} catch (Exception e) {
			errMsg = error(document, "Could not resolve requirement file: " + fileName);
		}

		if (modulePath != null) {
			try {
				int startLine = 0;

				Map<String, Object> attributes = AttributeParser.parse(attrs);
				int leveloffset = getLeveloffset(attributes);
				appendLeveloffset(strb, leveloffset, false);

				List<String> lines = Files.readAllLines(modulePath);
				for (int i = startLine; i < lines.size(); i++) {
					int relLineNumber = i - startLine + 1;
					if (isInSelectedLineRange(attributes, relLineNumber)) {
						strb.append(lines.get(i)).append("\n");
					}
				}

				appendLeveloffset(strb, leveloffset, true);
			} catch (ParseException e) {
				errMsg = error(document, "Could not parse attributes: " + attrs);
			} catch (IOException e) {
				errMsg = error(document, "Could not read requirement file: " + modulePath.toString());
			}
		}

		String result;
		if (errMsg != null) {
			result = apiInclude + "\n" + errMsg;
		} else {
			result = strb.toString();
		}

		return result;
	}

}
