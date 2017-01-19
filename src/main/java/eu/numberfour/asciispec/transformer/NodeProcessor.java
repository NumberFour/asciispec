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

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

/**
 * Processes Asciidoctor AST nodes of various types. Used in {@link ChildrenTransformer}. Each method in this interface
 * processes a particular kind of block and returns a list of blocks that should replace the given block in the AST.
 * Note that an implementation of this interface must not change the AST structure itself. All it should do is create
 * replacement nodes (or return the given node as is if it should not be replaced).
 */
public interface NodeProcessor {
	/**
	 * Processes the given section and returns a collection of nodes that will replace the given node in its parent's
	 * children.
	 *
	 * @param section
	 *            the section to process
	 * @param parent
	 *            the sections's parent node
	 * @return the replacement nodes
	 */
	public Collection<StructuralNode> process(Section section, StructuralNode parent);

	/**
	 * Processes the given block and returns a collection of nodes that will replace the given node in its parent's
	 * children.
	 *
	 * @param block
	 *            the block to process
	 * @param parent
	 *            the block's parent node
	 * @return the replacement nodes
	 */
	public Collection<StructuralNode> process(Block block, StructuralNode parent);

	/**
	 * Processes the given description list and returns a collection of nodes that will replace the given node in its
	 * parent's children.
	 *
	 * @param list
	 *            the list to process
	 * @param parent
	 *            the block's parent node
	 * @return the replacement nodes
	 */
	public Collection<StructuralNode> process(DescriptionList list, StructuralNode parent);

	/**
	 * Processes the given list and returns a collection of nodes that will replace the given node in its parent's
	 * children.
	 *
	 * @param list
	 *            the list to process
	 * @param parent
	 *            the block's parent node
	 * @return the replacement nodes
	 */
	public Collection<StructuralNode> process(List list, StructuralNode parent);

	/**
	 * Processes the given table and returns a collection of nodes that will replace the given node in its parent's
	 * children.
	 *
	 * @param table
	 *            the table to process
	 * @param parent
	 *            the block's parent node
	 * @return the replacement nodes
	 */
	public Collection<StructuralNode> process(Table table, StructuralNode parent);
}
