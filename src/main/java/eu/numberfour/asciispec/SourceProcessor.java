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
package eu.numberfour.asciispec;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes asciidoctor source while respecting commonly used methods to escape
 * content. The following methods of escaping content from processing are
 * supported:
 *
 * <ul>
 * <li>Block comment (delimited with <code>////</code>)</li>
 * <li>Inline comment (introduced by <code>//</code>)</li>
 * <li>Inline pass (e.g. <code>+++ this is ignored +++</code>)</li>
 * <li>Pass macro (e.g. <code>pass:[this is ignored]</code>) - note that the
 * ignored text must not contain square brackets!</li>
 * <li>Pass block (delimited with <code>++++</code>)</li>
 * <li>Source block (delimited with <code>```</code>)</li>
 * <li>Source block (delimited with <code>[source]</code> and / or
 * <code>----</code>
 * </ul>
 */
public class SourceProcessor {
	private final Set<String> ignoredBlockNames = new HashSet<>();
	private final Set<String> ignoredBlockTypes = new HashSet<>();

	private static final Pattern BLOCK_NAME_PATTERN = Pattern.compile("\\s*\\[([^,\\]]+).*?\\]\\s*");
	private static final Pattern[] BLOCK_PATTERNS = { Pattern.compile("\\s*(////)\\s*"), // Comment
			Pattern.compile("\\s*(====)\\s*"), // Example
			Pattern.compile("\\s*(```).*"), // Fenced block with parameter
			Pattern.compile("\\s*(----)\\s*"), // Listing
			Pattern.compile("\\s*(\\.\\.\\.\\.)\\s*"), // Literal
			Pattern.compile("\\s*(--)\\s*"), // Open
			Pattern.compile("\\s*(\\+\\+\\+\\+)\\s*"), // Pass
			Pattern.compile("\\s*(\\*\\*\\*\\*)\\s*"), // Sidebar
			Pattern.compile("\\s*(|===)\\s*"), // Tabular
			Pattern.compile("\\s*(____)\\s*") // Verse
	};

	/**
	 * A very simple implementation of the state pattern is used to track
	 * ignored blocks. The subclasses of this class represent the different
	 * types of ignored blocks.
	 */
	private abstract class BlockType {
		public abstract boolean isIgnored();

		public abstract BlockType next(String line);

		protected BlockType checkBlockPatterns(String line, boolean forceIgnore) {
			for (Pattern pattern : BLOCK_PATTERNS) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String blockDelimiter = matcher.group(1);
					if (forceIgnore || ignoredBlockTypes.contains(blockDelimiter))
						return new AnonymousBlockType(pattern);
				}
			}

			return this;
		}

		protected BlockType checkNamedBlockPattern(String line) {
			Matcher matcher = BLOCK_NAME_PATTERN.matcher(line);
			if (matcher.matches()) {
				String blockName = matcher.group(1);
				if (ignoredBlockNames.contains(blockName))
					return new NamedBlockType();
			}

			return this;
		}
	}

	/**
	 * The default block indicates that the last line was not in any block.
	 */
	private class DefaultBlockType extends BlockType {
		@Override
		public boolean isIgnored() {
			return false;
		}

		@Override
		public BlockType next(String line) {
			BlockType nextBlock = checkBlockPatterns(line, false);
			if (nextBlock != this)
				return nextBlock;

			return checkNamedBlockPattern(line);
		}
	}

	/**
	 * An anonymous block indicates that the last line was part of a block that
	 * was delimited by any of the preconfigured asciidoctor block delimiters.
	 */
	private class AnonymousBlockType extends BlockType {
		private final Pattern pattern;

		public AnonymousBlockType(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean isIgnored() {
			return true;
		}

		@Override
		public BlockType next(String line) {
			if (pattern.matcher(line).matches())
				return new DefaultBlockType();
			return this;
		}
	}

	/**
	 * A named block indicates that the last line was a named block declaration.
	 * The next line will immediately leave this block, since it will either be
	 * a delimiter of an anonymous block or the default block.
	 */
	private class NamedBlockType extends BlockType {
		@Override
		public boolean isIgnored() {
			return true;
		}

		@Override
		public BlockType next(String line) {
			BlockType nextBlock = checkBlockPatterns(line, true);
			if (nextBlock != this)
				return nextBlock;
			return new DefaultBlockType();
		}
	}

	/*
	 * The pass macro will not work with nested macros, e.g.
	 * <code>pass:[task:GH-40[]]</code>. Nested structures like that cannot be
	 * parsed with regular expressions.
	 */
	private static final Pattern INLINE_IGNORE_PATTERN = Pattern
			.compile("\\+\\+\\+.*?\\+\\+\\+|pass:[^\\[]*\\[[^\\]]*\\]");

	private final Function<String, List<String>> transform;

	private BlockType currentBlock;

	/**
	 * Creates a new processor.
	 * <p>
	 * When calling processing methods later, make sure you pass a transform
	 * function.
	 */
	public SourceProcessor() {
		this(null);
		initialize();
	}

	/**
	 * Creates a new processor that applies the given function to every
	 * non-ignored portion of each non-ignored line.
	 *
	 * @param transform
	 *            the function to apply
	 */
	public SourceProcessor(Function<String, List<String>> transform) {
		this.transform = transform;

		ignoredBlockNames.add("source");
		ignoredBlockTypes.add("```");
		ignoredBlockTypes.add("////");
		ignoredBlockTypes.add("++++");
		ignoredBlockTypes.add("----");
	}

	public boolean isIgnoringCurrentBlock() {
		return currentBlock.isIgnored();
	}

	/**
	 * Processes the given lines and returns the processed result.
	 *
	 * @param lines
	 *            the lines to process
	 * @return the processed lines
	 */
	public List<String> process(Iterable<String> lines) {
		final Iterator<String> iterator = lines.iterator();
		Supplier<String> supp = new Supplier<String>() {
			@Override
			public String get() {
				if (iterator.hasNext())
					return iterator.next();
				return null;
			}
		};

		return process(supp);
	}

	/**
	 * Processes the given lines and returns the processed result. Regions such
	 * as comments are ignored automatically.
	 *
	 * @param lineSupplier
	 *            supplies the lines to process
	 * @return the processed lines
	 */
	public List<String> process(Supplier<String> lineSupplier) {
		initialize();

		LinkedList<String> result = new LinkedList<>();
		for (String line = lineSupplier.get(); line != null; line = lineSupplier.get()) {
			if (shouldProcess(line, true)) {
				processLine(line, transform, result);
			} else {
				result.add(line);
			}
		}
		return result;
	}

	/**
	 * Processes the given line and returns the processed result. The transform
	 * function is given as a parameter.
	 * <p>
	 * 
	 * <b>Note:</b> The line input is not used for updating the current block
	 * ignored state. Thus, the update has to be toggled manually using the
	 * method {@link #updateBlockState(String)} to enable escaping for e.g.
	 * comment regions.
	 *
	 * @param line
	 *            a line to process
	 * @param transform
	 *            the transform function
	 * @return the processed lines
	 */
	public List<String> processWithoutUpdate(String line, Function<String, List<String>> transform) {
		LinkedList<String> result = new LinkedList<>();
		if (shouldProcess(line, false)) {
			processLine(line, transform, result);
		} else {
			result.add(line);
		}
		return result;
	}

	private void initialize() {
		currentBlock = new DefaultBlockType();
	}

	static private void processLine(String line, Function<String, List<String>> transform, LinkedList<String> result) {
		Objects.requireNonNull(transform);

		if (line.isEmpty()) {
			result.addAll(transform.apply(line));
			return;
		}
		if (line.startsWith("//")) {
			result.add(line);
			return;
		}

		LinkedList<String> newLines = new LinkedList<>();

		Matcher matcher = INLINE_IGNORE_PATTERN.matcher(line);

		int start = 0;
		while (matcher.find()) {
			int end = matcher.start();

			final String passPart = line.substring(matcher.start(), matcher.end());
			final String processPart = line.substring(start, end);
			if (!processPart.isEmpty())
				appendLines(newLines, transform.apply(processPart));
			appendLineFragment(newLines, passPart);

			start = matcher.end();
		}

		if (start < line.length()) {
			final String processPart = line.substring(start, line.length());
			appendLines(newLines, transform.apply(processPart));
		}

		result.addAll(newLines);
	}

	static private void appendLines(LinkedList<String> newLines, List<String> toAppend) {
		if (toAppend.isEmpty())
			return;

		if (newLines.isEmpty()) {
			newLines.addAll(toAppend);
		} else {
			appendLineFragment(newLines, toAppend.get(0));
			newLines.addAll(toAppend.subList(1, toAppend.size()));
		}
	}

	static private void appendLineFragment(LinkedList<String> lines, String fragment) {
		if (lines.isEmpty()) {
			lines.add(fragment);
		} else {
			lines.add(lines.pollLast() + fragment);
		}
	}

	private boolean shouldProcess(String line, boolean toggle) {
		boolean wasIgnored = currentBlock.isIgnored();
		if (toggle) {
			updateBlockState(line);
		}
		return !(wasIgnored || currentBlock.isIgnored());
	}

	/**
	 * Toggles whether the current block is ignored or not (see
	 * {@link #isIgnoringCurrentBlock()}). When using
	 * {@link #processWithoutUpdate(String, Function)}, the block state is not
	 * updated automatically. This update can be done using this method.
	 * 
	 * @param line
	 *            a line to process
	 */
	public void updateBlockState(String line) {
		currentBlock = currentBlock.next(line);
	}
}
