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
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;

import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;
import eu.numberfour.asciispec.math.MathService;

/**
 * This preprocessor processes the mathinclude::[] macro which is used to set up additional LaTeX commands that are made
 * available to the {@link InlineMathPreprocessor} and the {@link MathBlockProcessor}.
 */
public class MathIncludePreprocessor extends MacroPreprocessor<String> {
	private static final String MATH_INCLUDE_KEY = "mathinclude";
	private static final Pattern MATH_INCLUDE_PATTERN = Pattern.compile("\\s*mathinclude::(.*?)\\[[^\\]]*\\]\\s*");

	@Override
	public void init(Document document) {
		registerPattern(MATH_INCLUDE_KEY, MATH_INCLUDE_PATTERN);
	}

	@Override
	protected String processMatch(Document document, String key, Matcher matcher) {
		switch (key) {
		case MATH_INCLUDE_KEY:
			return processMathIncludeMatch(document, matcher);
		}

		return matcher.group();
	}

	private String processMathIncludeMatch(Document document, Matcher matcher) {
		final String target = matcher.group(1);
		if (target.trim().isEmpty()) {
			return error(document, "Invalid use of mathinclude macro: Missing or blank target");
		}

		try {
			File file = searchFileToInclude(document, target);
			Path path = file.toPath();

			MathService mathService = MathService.get(document);
			mathService.include(path);
			return "";
		} catch (FileNotFoundException | IllegalArgumentException e) {
			return error(document, "Unable to load LaTeX commands from '" + target + "': " + e.getMessage());
		}
	}

	private File searchFileToInclude(Document document, String target) throws FileNotFoundException {
		try {
			return searchFile(target);
		} catch (MultipleFileMatchesException e) {
			File firstMatch = e.matches.get(0);
			warn(document, "Found multiple matches for file '" + e.fileName + "', using '" + firstMatch + "'");
			return firstMatch;
		}
	}
}
