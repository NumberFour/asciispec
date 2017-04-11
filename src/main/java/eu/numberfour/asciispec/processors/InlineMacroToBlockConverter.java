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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.internal.RubyObjectWrapper;

import eu.numberfour.asciispec.AdocUtils;
import eu.numberfour.asciispec.AttributeParser;
import eu.numberfour.asciispec.issue.IssueAcceptor;
import eu.numberfour.asciispec.issue.IssuePrinter;
import eu.numberfour.asciispec.transformer.ChildrenTransformer;
import eu.numberfour.asciispec.transformer.NodeProcessor;

/**
 * Base class for a type of node processor that collects inline macros from a node, removes them, and appends their
 * output to a sibling block that is inserted right after the original node that was being processed. The processor
 * recursively visits each node of a document. It also handles list and table nodes.
 * <p>
 * The goal of this processor is to be able to extract information in the form of inline macros from a paragraph and to
 * display that information in an additional block that is inserted after the block being processed. This additional
 * block could be a sidebar block that is then positioned right next the block being processed.
 * </p>
 */
public class InlineMacroToBlockConverter extends Treeprocessor {

	private static abstract class DocumentTransformer implements NodeProcessor {

		private final Pattern macroPattern;
		private final Map<String, InlineMacroProcessor> macroProcessors;
		protected final Processor nodeFactory;
		protected final IssueAcceptor issueAcceptor;

		protected DocumentTransformer(Pattern macroPattern, Map<String, InlineMacroProcessor> macroProcessors,
				Processor nodeFactory, IssueAcceptor issueAcceptor) {
			this.macroPattern = Objects.requireNonNull(macroPattern);
			this.macroProcessors = Objects.requireNonNull(macroProcessors);
			this.nodeFactory = Objects.requireNonNull(nodeFactory);
			this.issueAcceptor = Objects.requireNonNull(issueAcceptor);
		}

		@Override
		public Collection<StructuralNode> process(Section section, StructuralNode parent) {
			transformChildren(section);
			return Collections.singletonList(section);
		}

		@Override
		public Collection<StructuralNode> process(Block block, StructuralNode parent) {
			transformChildren(block);

			if (!block.isSubstitutionEnabled(StructuralNode.SUBSTITUTION_MACROS))
				return Collections.singletonList(block);

			List<String> input = block.getLines();
			List<String> siblingNodeContent = new LinkedList<>();
			List<String> newNodeContent = processLines(input, parent, siblingNodeContent);

			List<String> substitutions = new LinkedList<>();
			for (String substitution : block.getSubstitutions())
				substitutions.add(":" + substitution);

			Map<Object, Object> options = new HashMap<>();
			options.put("subs", substitutions);
			options.put(ContentModel.KEY, ":" + block.getContentModel());

			Block newBlock = nodeFactory.createBlock(parent, block.getContext(), newNodeContent, block.getAttributes(),
					options);
			AdocUtils.reparentChildren(block, newBlock);

			return createResult(newBlock, parent, siblingNodeContent);
		}

		@Override
		public Collection<StructuralNode> process(org.asciidoctor.ast.List list, StructuralNode parent) {
			List<String> siblingNodeContent = new LinkedList<>();
			List<StructuralNode> items = list.getItems();
			if (items != null) {
				for (StructuralNode item : items)
					processListItem((ListItem) item, siblingNodeContent);
			}

			return createResult(list, parent, siblingNodeContent);
		}

		/**
		 * Processes the given list item. If the list item's text contains an inline macro that is processed by one of
		 * the registered inline macro processors, then that processor's output is added to the given list of strings.
		 *
		 * @param item
		 *            the item to process
		 * @param newSiblingNodeContent
		 *            list to add the new sibling node content to
		 */
		private void processListItem(ListItem item, List<String> newSiblingNodeContent) {
			transformChildren(item);

			String text = AdocUtils.getNodeProperty((RubyObjectWrapper) item, "text");
			if (text != null) {
				String newText = processLine(text, item, newSiblingNodeContent);
				if (newText != text)
					AdocUtils.setNodeProperty(item, "text", newText);
			}
		}

		@Override
		public Collection<StructuralNode> process(DescriptionList list, StructuralNode parent) {
			List<String> siblingNodeContent = new LinkedList<>();
			List<DescriptionListEntry> items = list.getItems();
			if (items != null) {
				for (DescriptionListEntry item : items)
					processDescriptionListItem(item, siblingNodeContent);
			}

			return createResult(list, parent, siblingNodeContent);
		}

		private void processDescriptionListItem(DescriptionListEntry item, List<String> siblingNodeContent) {
			for (ListItem term : item.getTerms())
				processListItem(term, siblingNodeContent);
		}

		@Override
		public Collection<StructuralNode> process(Table table, StructuralNode parent) {
			List<String> siblingNodeContent = new LinkedList<>();

			processTableRows(table.getHeader(), siblingNodeContent);
			processTableRows(table.getBody(), siblingNodeContent);

			return createResult(table, parent, siblingNodeContent);
		}

		private void processTableRows(List<Row> rows, List<String> newSiblingNodeContent) {
			for (Row row : rows) {
				for (Cell cell : row.getCells())
					processTableCell(cell, newSiblingNodeContent);
			}
		}

		private void processTableCell(Cell cell, List<String> newSiblingNodeContent) {
			Document innerDocument = cell.getInnerDocument();
			if (innerDocument != null) {
				InnerDocumentTransformer nodeTransformer = new InnerDocumentTransformer(macroPattern, macroProcessors,
						nodeFactory, issueAcceptor);
				new ChildrenTransformer<>(innerDocument, nodeTransformer).transform();
				newSiblingNodeContent.addAll(nodeTransformer.getSiblingNodeContents());
			}

			String text = AdocUtils.getNodeProperty((RubyObjectWrapper) cell, "text");
			if (text != null) {
				String newText = processLine(text, cell, new LinkedList<String>());
				if (newText != text)
					AdocUtils.setNodeProperty(cell, "text", newText);
			}
		}

		private void transformChildren(StructuralNode node) {
			new ChildrenTransformer<>(node, this).transform();
		}

		/**
		 * Processes several lines by running the registered inline macro processors on the given lines. If the inline
		 * macro processors produce new output, that output is appended to the given list of lines.
		 *
		 * @param lines
		 *            the lines to process
		 * @param parent
		 *            the parent of the block node containing the given line
		 * @param newSiblingNodeContent
		 *            the list of lines to append the output of the registered inline macro processors to
		 * @return the processed lines
		 */
		private List<String> processLines(List<String> lines, ContentNode parent, List<String> newSiblingNodeContent) {
			return AdocUtils.processLines(lines, (String nodeLineFragment) -> {
				StringBuilder newNodeLineFragment = new StringBuilder();
				Matcher matcher = macroPattern.matcher(nodeLineFragment);

				int last = 0;
				while (matcher.find()) {
					int cur = matcher.start();
					newNodeLineFragment.append(nodeLineFragment.substring(last, cur));

					try {
						String name = matcher.group(1);
						String target = matcher.group(2);
						String attributeStr = matcher.group(3);
						Map<String, Object> attributes = AttributeParser.parse(attributeStr);

						String newBlockLine = processMacro(parent, name, target, attributes);
						if (newBlockLine != null)
							newSiblingNodeContent.add(newBlockLine);
					} catch (Exception e) {
						issueAcceptor.error(parent, e.getMessage());
						newNodeLineFragment.append(nodeLineFragment.substring(cur, matcher.end()));
					}

					last = matcher.end();
				}

				newNodeLineFragment.append(nodeLineFragment.substring(last));

				LinkedList<String> result = new LinkedList<>();
				result.add(newNodeLineFragment.toString());
				return result;
			});
		}

		/**
		 * Processes one line of a block node by running the registered inline macro processors on the given line. If
		 * the inline macro processors produce new output, that output is appended to the given list of lines.
		 *
		 * @param line
		 *            the line to process
		 * @param parent
		 *            the parent of the block node containing the given line
		 * @param newSiblingNodeContent
		 *            the list of lines to append the output of the registered inline macro processors to
		 * @return the processed line
		 */
		private String processLine(String line, ContentNode parent, List<String> newSiblingNodeContent) {
			return processLines(Collections.singletonList(line), parent, newSiblingNodeContent).get(0);
		}

		/**
		 * Processes an inline macro that was extracted from a paragraph block. Returns one line of Asciidoc text that
		 * should be added to the newly created sibling node.
		 *
		 * @param parent
		 *            the parent block that contains the inline macro
		 * @param name
		 *            the name of the inline macro being processed
		 * @param target
		 *            the target of the inline macro being processed
		 * @param attributes
		 *            the attributes that were passed to the macro
		 * @return a line of Asciidoc text that should be added to the new sibling node
		 */
		private String processMacro(ContentNode parent, String name, String target,
				Map<String, Object> attributes) {
			InlineMacroProcessor processor = macroProcessors.get(name);
			return processor.process(parent, target, attributes).toString();
		}

		private List<StructuralNode> createResult(StructuralNode node, StructuralNode parent,
				List<String> siblingNodeContent) {
			List<StructuralNode> result = new LinkedList<>();
			result.add(node);

			if (!siblingNodeContent.isEmpty()) {
				StructuralNode newSibling = onNodeProcessed(parent, siblingNodeContent);
				if (newSibling != null)
					result.add(newSibling);
			}
			return result;
		}

		protected abstract StructuralNode onNodeProcessed(StructuralNode parent, List<String> siblingNodeContent);
	}

	private static class OuterDocumentTransformer extends DocumentTransformer {
		private final String siblingNodeContext;

		public OuterDocumentTransformer(Pattern macroPattern, Map<String, InlineMacroProcessor> macroProcessors,
				Processor nodeFactory, IssueAcceptor issueAcceptor, String siblingNodeContext) {
			super(macroPattern, macroProcessors, nodeFactory, issueAcceptor);
			this.siblingNodeContext = siblingNodeContext;
		}

		@Override
		protected StructuralNode onNodeProcessed(StructuralNode parent, List<String> siblingNodeContent) {
			return nodeFactory.createBlock(parent, siblingNodeContext, siblingNodeContent);
		}
	}

	private static class InnerDocumentTransformer extends DocumentTransformer {

		private final List<String> siblingNodeContents = new LinkedList<>();

		protected InnerDocumentTransformer(Pattern macroPattern, Map<String, InlineMacroProcessor> macroProcessors,
				Processor nodeFactory, IssueAcceptor issueAcceptor) {
			super(macroPattern, macroProcessors, nodeFactory, issueAcceptor);
		}

		@Override
		protected StructuralNode onNodeProcessed(StructuralNode parent, List<String> siblingNodeContent) {
			siblingNodeContents.addAll(siblingNodeContent);
			return null;
		}

		public List<String> getSiblingNodeContents() {
			return siblingNodeContents;
		}
	}

	private final String siblingNodeContext;
	private Pattern macroPattern;
	private final Map<String, InlineMacroProcessor> processors;

	// TODO: make this configurable
	private final IssueAcceptor issueAcceptor = new IssuePrinter();

	/**
	 * Creates a new instance of this processor that creates a new sibling node with the given context.
	 *
	 * @param siblingNodeContext
	 *            the context of the sibling node to create
	 */
	protected InlineMacroToBlockConverter(String siblingNodeContext) {
		this.siblingNodeContext = siblingNodeContext;
		this.processors = new HashMap<>();
	}

	/**
	 * Registers an inline macro processor to be called by this converter.
	 *
	 * @param macroName
	 *            the name of the macro to process using the given processor
	 * @param processor
	 *            the processor
	 */
	public void registerInlineMacroProcessor(String macroName, InlineMacroProcessor processor) {
		if (processors.containsKey(macroName))
			throw new IllegalArgumentException("Processor already registered.");
		processors.put(Objects.requireNonNull(macroName), Objects.requireNonNull(processor));
		String macroNamesPattern = processors.keySet().stream().collect(Collectors.joining("|"));
		macroPattern = Pattern
				.compile("(" + macroNamesPattern + "):([^\\[\\s]*)\\[([^\\]]*)\\]");
	}

	@Override
	public Document process(Document document) {
		OuterDocumentTransformer nodeTransformer = new OuterDocumentTransformer(macroPattern, processors, this,
				issueAcceptor, siblingNodeContext);
		new ChildrenTransformer<>(document, nodeTransformer).transform();

		resetConfigFinalized();
		return document;
	}

	/**
	 * @see HackProcessors#resetConfigFinalized(Processor)
	 */
	private void resetConfigFinalized() {
		HackProcessors.resetConfigFinalized(this);
		for (InlineMacroProcessor imp : processors.values()) {
			HackProcessors.resetConfigFinalized(imp);
		}
	}
}
