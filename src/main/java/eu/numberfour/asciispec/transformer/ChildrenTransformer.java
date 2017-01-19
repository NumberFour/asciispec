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
package eu.numberfour.asciispec.transformer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

/**
 * Applies a transformation to a subtree of the Asciidoctor AST. Specifically, this class rewrites the children of a
 * given AST node using an instance of {@link NodeProcessor}. Note that this transformer is not applied recursively. The
 * decision whether to recurse or not is left to the node processor.
 */
public class ChildrenTransformer<T extends StructuralNode> {
	private final T node;
	private final NodeProcessor nodeProcessor;

	private final List<StructuralNode> newChildren;

	/**
	 * Creates a new transformer that rewrites the children of the given node using the given node transformer.
	 *
	 * @param node
	 *            the node to transform
	 * @param nodeProcessor
	 *            the node processor to apply to the children
	 */
	public ChildrenTransformer(T node, NodeProcessor nodeProcessor) {
		this.node = Objects.requireNonNull(node);
		this.nodeProcessor = Objects.requireNonNull(nodeProcessor);
		this.newChildren = new LinkedList<>();
	}

	/**
	 * Performs the transformation.
	 */
	public void transform() {
		List<StructuralNode> children = node.getBlocks();

		for (StructuralNode child : children)
			transformChild(child);

		children.clear();
		children.addAll(newChildren);
	}

	private void transformChild(StructuralNode child) {
		if (child instanceof Section)
			transformChildSection((Section) child);
		else if (child instanceof Block)
			transformChildBlock((Block) child);
		else if (child instanceof org.asciidoctor.ast.List)
			transformChildList((org.asciidoctor.ast.List) child);
		else if (child instanceof DescriptionList)
			transformChildDescriptionList((DescriptionList) child);
		else if (child instanceof Table)
			transformChildTable((Table) child);
		else
			addChild(child);
	}

	private void transformChildSection(Section section) {
		addChildren(nodeProcessor.process(section, node));
	}

	private void transformChildBlock(Block block) {
		addChildren(nodeProcessor.process(block, node));
	}

	private void transformChildList(org.asciidoctor.ast.List list) {
		addChildren(nodeProcessor.process(list, node));
	}

	private void transformChildDescriptionList(DescriptionList list) {
		addChildren(nodeProcessor.process(list, node));
	}

	private void transformChildTable(Table table) {
		addChildren(nodeProcessor.process(table, node));
	}

	private void addChildren(Collection<? extends StructuralNode> children) {
		newChildren.addAll(children);
	}

	private void addChild(StructuralNode child) {
		newChildren.add(child);
	}
}
