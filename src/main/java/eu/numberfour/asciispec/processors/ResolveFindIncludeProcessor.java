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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;

import eu.numberfour.asciispec.findresolver.IgnoreFileException;
import eu.numberfour.asciispec.findresolver.InconsistentUseOfModifiersException;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This {@link IncludeProcessor} evaluates all include macros in the document which start with <code>{find}</code>. The
 * include macro support the TARGET_ONCE modifier: <br/>
 * <br/>
 * TARGET_ONCE - (Alias: ONCE) The file with the specified target is included at the first site where the target was
 * specified.</li> issued.
 */
public class ResolveFindIncludeProcessor extends ResolveIncludeProcessor {

	/** Given file is included only once */
	private static final String MODIFIER_TARGET_ONCE = "TARGET_ONCE";
	private static final String MODIFIER_TARGET_ONCE_ALIAS = "ONCE";

	private static final String INCLUDE_FIND = "find";

	private final Set<String> allIncludedTargets = new HashSet<>();
	private final Set<String> targets = new HashSet<>();

	/**
	 * Constructor
	 */
	public ResolveFindIncludeProcessor() {
		super(INCLUDE_FIND);
	}

	@Override
	protected File findFile(Document document, Map<String, Object> attributes, File containerFile, String target,
			String line) throws FileNotFoundException, IgnoreFileException {

		try {
			if (!target.endsWith(".adoc"))
				target = target + ".adoc";

			try {
				checkTargetOnce(attributes, target);
			} catch (InconsistentUseOfModifiersException e) {
				warn(document, e.getMessage());
				if (e.hasIgnoreFileException())
					throw e.ignoreFileException;
			}

			File file = searchFile(target);
			// File file = FileStackHelper.searchRelativeTo(target,
			// getCurrentDir(), getBasePath());
			allIncludedTargets.add(target);
			return file;
		} catch (MultipleFileMatchesException e) {
			warn(document, e.getMessage());
			File file = e.matches.get(0);
			return file;
		}
	}

	private void checkTargetOnce(Map<String, Object> attributes, String target)
			throws IgnoreFileException, InconsistentUseOfModifiersException {

		if (isTargetOnce(attributes) && allIncludedTargets.contains(target) && !targets.contains(target)) {
			IgnoreFileException ife = new IgnoreFileException();
			throw new InconsistentUseOfModifiersException(ife, MODIFIER_TARGET_ONCE, target);
		}

		if (isTargetOnce(attributes)) {
			if (targets.contains(target)) {
				throw new IgnoreFileException();
			}
			targets.add(target);
		}
		if (!isTargetOnce(attributes) && targets.contains(target)) {
			throw new InconsistentUseOfModifiersException(MODIFIER_TARGET_ONCE, target);
		}

	}

	private boolean isTargetOnce(Map<String, Object> attributes) {
		boolean isTargetOnce = attributes.values().contains(MODIFIER_TARGET_ONCE);
		isTargetOnce |= attributes.values().contains(MODIFIER_TARGET_ONCE_ALIAS);
		return isTargetOnce;
	}

	@Override
	protected Map<String, Object> getNewAttributes(Map<String, Object> attributes) {
		return clearAttributes(attributes, MODIFIER_TARGET_ONCE, MODIFIER_TARGET_ONCE_ALIAS);
	}

}
