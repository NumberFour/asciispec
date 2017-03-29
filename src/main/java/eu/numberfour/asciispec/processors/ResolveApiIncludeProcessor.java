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
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;

import eu.numberfour.asciispec.findresolver.FileStackHelper;
import eu.numberfour.asciispec.findresolver.IgnoreFileException;
import eu.numberfour.asciispec.findresolver.MultipleFileMatchesException;

/**
 * This {@link IncludeProcessor} evaluates all include macros in the document
 * which start with <code>{api}</code>.
 */
public class ResolveApiIncludeProcessor extends ResolveIncludeProcessor implements SourceLinkMixin {

	private static final String INCLUDE_API = "api";

	private final SourceLinkMixinState state = new SourceLinkMixinState();
	private File indexFile;

	/**
	 * Constructor
	 */
	public ResolveApiIncludeProcessor() {
		super(INCLUDE_API);
	}

	@Override
	protected File findFile(Document document, Map<String, Object> attributes, File containerFile, String target,
			String line) throws FileNotFoundException, IgnoreFileException {

		try {

			File file = FileStackHelper.searchRelativeTo(target, getCurrentFile(), getBasePath());
			return file;
		} catch (MultipleFileMatchesException e) {
			warn(document, e.getMessage());
			File file = e.matches.get(0);
			return file;
		}
	}


	@Override
	protected Map<String, Object> getNewAttributes(Map<String, Object> attributes) {
		// FIXME: adjust LINES attribute
		return attributes;
	}

	@Override
	public SourceLinkMixinState getState() {
		return state;
	}

	@Override
	public File getIndexFile() {
		return indexFile;
	}

	@Override
	public String getIndexFileName() {
		// TODO Auto-generated method stub
		return null;
	}

}
