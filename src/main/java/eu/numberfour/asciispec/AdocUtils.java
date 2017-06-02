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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.ast.NodeConverter.NodeType;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.ast.impl.ContentNodeImpl;
import org.asciidoctor.ast.impl.ListImpl;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyObjectWrapper;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

import com.google.common.collect.Iterables;

/**
 * A collection of commonly used utility methods for AsciiDoctorJ processors.
 */
public final class AdocUtils {

	private static final Pattern MULTILINE_COMMENT = Pattern.compile("\\s*////.*");

	private AdocUtils() {
		// no instances of this class
	}

	/**
	 * Returns a string representation of an inline macro with the given parameters. The representation is built as
	 * follows:
	 *
	 * <pre>
	 * <name>:<target>[<name1>="<value1>", <name2>="<value2>", ...]
	 * </pre>
	 *
	 * The name / value pairs are taken from the given attributes map. The values are always enclosed in double
	 * quotation marks.
	 *
	 * @param name
	 *            the macro name
	 * @param target
	 *            the macro target
	 * @param attributes
	 *            the attributes
	 * @return the string representation
	 */
	public static String inlineMacroToString(String name, String target, Map<String, Object> attributes) {
		StringBuilder result = new StringBuilder();
		result.append(name).append(":").append(target);
		result.append("[");
		result.append(attributes.entrySet().stream().map((entry) -> entry.getKey() + "=\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(", ")));
		result.append("]");
		return result.toString();
	}

	/**
	 * Applies the given transformation function line-wise to the given list of lines, taking into account comments.
	 *
	 * @see SourceProcessor
	 *
	 * @param lines
	 *            the lines to transform
	 * @param transform
	 *            the transformation function
	 * @return the transformed lines
	 */
	public static List<String> processLines(List<String> lines, Function<String, List<String>> transform) {
		return new SourceProcessor(transform).process(lines);
	}

	/**
	 * Applies the given transformation function line-wise to the given list of lines, taking into account comments.
	 *
	 * @see SourceProcessor
	 *
	 * @param lineSupplier
	 *            supplies the lines to transform
	 * @param transform
	 *            the transformation function
	 * @return the transformed lines
	 */
	public static List<String> processLines(Supplier<String> lineSupplier, Function<String, List<String>> transform) {

		return new SourceProcessor(transform).process(lineSupplier);
	}

	/**
	 * Applies the given transformation function to the given line, taking into account comments.
	 *
	 * @see SourceProcessor
	 *
	 * @param line
	 *            the line to transform
	 * @param transform
	 *            the transformation function
	 * @return the transformed lines
	 */
	public static List<String> processLine(String line, Function<String, List<String>> transform) {
		return new SourceProcessor(transform).process(line);
	}

	/**
	 * Searches the given lines using the given matcher and returns the matcher's non-<code>null</code> result. The
	 * given matcher function is not applied to any commented parts of the given lines. The result of this function is
	 * the first non-<code>null</code> result of the given matcher function, which is repeatedly invoked for each line.
	 *
	 * @param lines
	 *            the lines to search
	 * @param matcher
	 *            the matcher to use
	 * @return the first non-<code>null</code> result of the given matcher or <code>null</code> if the matcher did not
	 *         match any line
	 */
	public static <R> R searchLines(final List<String> lines, Function<String, R> matcher) {
		boolean multilineComment = false;
		for (String line : lines) {
			if (MULTILINE_COMMENT.matcher(line).matches()) {
				multilineComment = !multilineComment;
				continue;
			}

			if (multilineComment)
				continue;

			String nonCommentedPortion = line;
			if (line.startsWith("//")) { // comments have to start at the
											// beginning of a line
				nonCommentedPortion = "";
			}

			R result = matcher.apply(nonCommentedPortion);
			if (result != null)
				return result;
		}

		return null;
	}

	/**
	 * Sanitizes the given string so that it can be used as an anchor. This involves replacing any non-word character
	 * with an underscore. A character is a non-word character if it is not in the character class
	 * <code>[a-zA-Z_0-9]</code>.
	 *
	 * @param str
	 *            the string to sanitize
	 * @return the sanitized string
	 */
	public static String sanitizeString(String str) {
		return Objects.requireNonNull(str).toLowerCase().replaceAll("\\W", "_");
	}

	/**
	 * Creates a phrase node that will convert to a link with an icon.
	 *
	 * @param processor
	 *            the processor to use as a node factory
	 * @param parent
	 *            the parent node in the document AST
	 * @param linkUrl
	 *            the link target URL
	 * @param linkText
	 *            the link text
	 * @param linkTitle
	 *            the link title
	 * @param linkClass
	 *            the role of the link
	 * @param iconName
	 *            the icon name
	 * @return the phrase node
	 */
	public static PhraseNode createLinkWithIcon(Processor processor, ContentNode parent, String linkUrl,
			String linkText, String linkTitle, String linkClass, String iconName) {
		String iconText = createIcon(processor, parent, iconName).convert();
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("role", linkClass);
		return createLink(processor, parent, linkUrl, iconText + linkText, linkTitle, attrs);
	}

	/**
	 * Creates a phrase node that will convert to a link with an icon.
	 *
	 * @param processor
	 *            the processor to use as a node factory
	 * @param parent
	 *            the parent node in the document AST
	 * @param linkUrl
	 *            the link target URL
	 * @param linkText
	 *            the link text
	 * @param linkTitle
	 *            the link title
	 * @param iconName
	 *            the icon name
	 * @return the phrase node
	 */
	public static PhraseNode createLinkWithIcon(Processor processor, ContentNode parent, String linkUrl,
			String linkText, String linkTitle, String iconName) {
		String iconText = createIcon(processor, parent, iconName).convert();
		return createLink(processor, parent, linkUrl, iconText + linkText, linkTitle);
	}

	/**
	 * Creates a phrase node that will convert to a link with the given parameters.
	 *
	 * @param processor
	 *            the processor to use as a node factory
	 * @param parent
	 *            the parent node in the document AST
	 * @param url
	 *            the target URL
	 * @param text
	 *            the link text
	 * @param title
	 *            the link title
	 * @return the phrase node
	 */
	public static PhraseNode createLink(Processor processor, ContentNode parent, String url, String text,
			String title) {
		return createLink(processor, parent, url, text, title, Collections.emptyMap());
	}

	/**
	 * Creates a phrase node that will convert to a link with the given parameters.
	 *
	 * @param processor
	 *            the processor to use as a node factory
	 * @param parent
	 *            the parent node in the document AST
	 * @param url
	 *            the target URL
	 * @param text
	 *            the link text
	 * @param title
	 *            the link title
	 * @param moreAttrs
	 *            more attributes of the link
	 * @return the phrase node
	 */
	public static PhraseNode createLink(Processor processor, ContentNode parent, String url, String text, String title,
			Map<String, Object> moreAttrs) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("title", title);
		attributes.putAll(moreAttrs);

		Map<String, Object> options = new HashMap<>();
		options.put("type", ":link");
		options.put("target", url);

		return processor.createPhraseNode(parent, "anchor", text, attributes, options);
	}

	/**
	 * Creates a phrase node that will convert to an icon with the given icon name.
	 *
	 * @param processor
	 *            the processor instance to use as the node factory
	 * @param parent
	 *            the parent node in the document AST
	 * @param iconName
	 *            the name of the icon
	 * @return the phrase node
	 */
	public static PhraseNode createIcon(Processor processor, ContentNode parent, String iconName) {
		Map<String, Object> options = new HashMap<>();
		options.put("type", "image");
		options.put("target", iconName);

		return processor.createPhraseNode(parent, "image", null, Collections.emptyMap(), options);
	}

	/**
	 * Create a link using Asciidoc syntax. The result is formatted as follows:
	 *
	 * <code>
	 * <<target,text>>
	 * </code>
	 *
	 * whereby <code>target</code> and <code>text</code> are substituted with the values of the respective parameters.
	 *
	 * @param target
	 *            the link target
	 * @param text
	 *            the link text
	 * @return the link
	 */
	public static String createLink(String target, String text) {
		StringBuilder result = new StringBuilder();
		appendLink(result, target, text);
		return result.toString();
	}

	/**
	 * Adds a link in Asciidoc syntax to the given string builder.
	 *
	 * @param result
	 *            the string buidler to append to
	 * @param target
	 *            the link target
	 * @param text
	 *            the link text
	 *
	 * @see #createLink(String, String)
	 */
	public static void appendLink(StringBuilder result, String target, String text) {
		result.append("<<").append(target).append(",").append(text).append(">>");
	}

	/**
	 * Create a link with title using Asciidoc syntax. The result is formatted as follows:
	 *
	 * <code>
	 * link:target[text,title="value"]
	 * </code>
	 *
	 * @param target
	 *            the link target
	 * @param text
	 *            the link text
	 * @param title
	 *            the link title
	 * @return the link
	 */
	public static String createLinkWithTitle(String target, String text, String title) {
		if (text.matches("[^\\\\][\\[\\]]|^\\[|^\\]"))
			throw new IllegalArgumentException("Title must not contain square brackets");

		return new StringBuilder("link:").append(target).append("[").append("\"").append(text).append("\"").append(",")
				.append("title=").append("\"").append(title).append("\"").append("]").toString();
	}

	/**
	 * Create an image link with title using Asciidoc syntax. The result is formatted as follows:
	 *
	 * <code>
	 * image:target[alt-text,title="value",link="value"]
	 * </code>
	 *
	 * @param target
	 *            the image link icon path
	 * @param title
	 *            the image link text
	 * @param linkUrl
	 *            the image link target
	 * @return the link
	 */
	public static String createImageWithLink(String target, String title, String linkUrl) {
		if (title.matches("[^\\\\][\\[\\]]|^\\[|^\\]"))
			throw new IllegalArgumentException("Title must not contain square brackets");

		return new StringBuilder("image:").append(target).append("[").append("\"").append(title).append("\"")
				.append(",").append("title=").append("\"").append(title).append("\"").append(",").append("link=")
				.append("\"").append(linkUrl).append("\"").append("]").toString();
	}

	/**
	 * Creates an anchor with the given name in Asciidoc syntax.
	 *
	 * @see #createAnchor(String)
	 *
	 * @param name
	 *            the name of the anchor (i.e., the link target)
	 * @return the anchor
	 */
	public static String createAnchor(String name) {
		StringBuilder result = new StringBuilder();
		appendAnchor(result, name);
		return result.toString();
	}

	/**
	 * Appends an anchor with the given name in Asciidoc syntax to the given string builder.
	 *
	 * @param result
	 *            the string builder to append to
	 * @param name
	 *            the name of the anchor (i.e., the link target)
	 *
	 * @see #createAnchor(String)
	 */
	public static void appendAnchor(StringBuilder result, String name) {
		result.append("[[").append(name).append("]]");
	}

	/**
	 * Creates a preformatted header string.
	 *
	 * @see #appendHeader(StringBuilder, String, String)
	 *
	 * @param type
	 *            the type of headline
	 * @param title
	 *            the title
	 * @return the header string
	 */
	public static String createHeader(String type, String title) {
		StringBuilder result = new StringBuilder();
		appendHeader(result, type, title);
		return result.toString();
	}

	/**
	 * Creates a preformatted header string.
	 *
	 * @see #appendHeader(StringBuilder, String, String, String)
	 *
	 * @param type
	 *            the type of headline
	 * @param title
	 *            the title
	 * @param id
	 *            the id
	 * @return the header string
	 */
	public static String createHeader(String type, String title, String id) {
		StringBuilder result = new StringBuilder();
		appendHeader(result, type, title, id);
		return result.toString();
	}

	/**
	 * Appends a preformatted header string to the given string builder using the sanitized version of the given title
	 * as the id.
	 *
	 * @param result
	 *            the string builder to append to
	 * @param type
	 *            the type of headline
	 * @param title
	 *            the title
	 *
	 * @see #appendHeader(StringBuilder, String, String, String)
	 */
	public static void appendHeader(StringBuilder result, String type, String title) {
		appendHeader(result, type, title, sanitizeString(title));
	}

	/**
	 * Appends a preformatted header string to the given string builder. The format is, in Asciidoc syntax:
	 *
	 * <pre>
	 * [[id]]*type:* <<id,title>>
	 * </pre>
	 *
	 * @param result
	 *            the string builder to append to
	 * @param type
	 *            the type of headline
	 * @param title
	 *            the title
	 * @param id
	 *            the id
	 */
	public static void appendHeader(StringBuilder result, String type, String title, String id) {
		appendAnchor(result, id);
		result.append("*").append(type).append(":* ");
		appendLink(result, id, title);
	}

	/**
	 * Creates a new list item with the given text and adds it to the given list.
	 *
	 * @param list
	 *            the list that should contain the newly created list item
	 * @param text
	 *            the text of the list item to create
	 * @return the newly created list item
	 */
	public static ListItem createListItem(org.asciidoctor.ast.List list, String text) {
		Ruby rubyRuntime = JRubyRuntimeContext.get(list);

		IRubyObject[] parameters = { ((ListImpl) list).getRubyObject(),
				text != null ? rubyRuntime.newString(text) : rubyRuntime.getNil() };

		return (ListItem) NodeConverter.createASTNode(rubyRuntime, NodeType.LIST_ITEM_CLASS, parameters);
	}

	/**
	 * Gets a property value of a Ruby object represented by the given content node. Note that this method circumvents
	 * any attribute readers defined in the Ruby class and returns the raw value of the property.
	 *
	 * @param node
	 *            the node
	 * @param propertyName
	 *            the name of the property to get
	 * @return the value of the property converted to a Java string
	 */
	public static String getNodeProperty(RubyObjectWrapper node, String propertyName) {
		RubyObjectWrapper wrapper = node;
		return wrapper.getRubyProperty("@" + propertyName).asJavaString();
	}

	/**
	 * Sets a property on a Ruby object represented by the given content node.
	 *
	 * @param node
	 *            the node
	 * @param propertyName
	 *            the name of the property to set
	 * @param propertyValue
	 *            the new value of the property
	 */
	public static void setNodeProperty(ContentNode node, String propertyName, String propertyValue) {
		RubyObjectWrapper wrapper = (RubyObjectWrapper) node;
		Ruby rubyRuntime = JRubyRuntimeContext.get(node);

		wrapper.setRubyProperty("@" + propertyName, RubyString.newString(rubyRuntime, propertyValue));
	}

	/**
	 * Moves all children from the given node to the given new parent.
	 *
	 * @param oldParent
	 *            the node whose children should be moved
	 * @param newParent
	 *            the new parent node to move the children to
	 */
	public static void reparentChildren(StructuralNode oldParent, StructuralNode newParent) {
		List<StructuralNode> children = new LinkedList<>(oldParent.getBlocks());
		oldParent.getBlocks().clear();
		setParent(children, newParent);
		newParent.getBlocks().addAll(children);
	}

	/**
	 * Moves the given node from its old parent to a new parent node.
	 *
	 * @param node
	 *            the node to move
	 * @param oldParent
	 *            the old parent of the given node
	 * @param newParent
	 *            the new parent to move the node to
	 *
	 * @throws IllegalArgumentException
	 *             if the given node is not a child of the given old parent
	 */
	public static void reparent(StructuralNode node, StructuralNode oldParent, StructuralNode newParent) {
		if (!oldParent.getBlocks().remove(node))
			throw new IllegalArgumentException("Node '" + node + "' is not a child of '" + newParent + "'");
		setParent(node, newParent);
		newParent.getBlocks().add(node);
	}

	/**
	 * Sets the parent of all nodes in the given collection to the given new parent. Note that this method does not
	 * remove the nodes in the collection from their old parent(s).
	 *
	 * @param nodes
	 *            the nodes whose parent should be set
	 * @param newParent
	 *            the new parent node
	 */
	public static void setParent(Collection<? extends ContentNode> nodes, ContentNode newParent) {
		for (ContentNode node : nodes)
			setParent(node, newParent);
	}

	/**
	 * Sets the parent of the given node to the given new parent. Note that this method does not remove the given node
	 * from its old parent.
	 *
	 * @param node
	 *            the node whose parent should be set
	 * @param newParent
	 *            the new parent node
	 */
	public static void setParent(ContentNode node, ContentNode newParent) {
		((ContentNodeImpl) node).setRubyProperty("parent", ((ContentNodeImpl) newParent).getRubyObject());
	}

	/**
	 * Returns the path to the folder that contains the given document.
	 *
	 * @param document
	 *            the document
	 * @return the path to the folder that contains the given document or <code>null</code> if that path could not be
	 *         determined
	 */
	public static Path getDocumentBasePath(ContentNode document) {
		String basePathStr = getAttributeAsString(Objects.requireNonNull(document), "docdir", null);
		if (basePathStr == null)
			return null;
		return Paths.get(basePathStr);
	}

	/**
	 * Returns the base file of the document.
	 *
	 * @param document
	 *            the document
	 * @return the path to the folder that contains the given document or <code>null</code> if that path could not be
	 *         determined
	 */
	public static File getDocumentBaseFile(ContentNode document) {
		String baseFileName = getAttributeAsString(Objects.requireNonNull(document), "docfile", null);
		if (baseFileName == null)
			return null;

		return new File(baseFileName);
	}

	/**
	 * Implements a standard way of dealing with relative paths in our custom asciidoc macros.
	 * <p>
	 * If the given path is relative, this method will try to {@link Path#resolve(Path) resolve} it against the
	 * {@link #getDocumentBasePath(ContentNode) document base path} and all its parent folders up to the file system's
	 * root folder until an existing file/folder is found and will return the absolute path to the first file/folder
	 * found or <code>null</code> if none was found.
	 * <p>
	 * If, on the other hand, the given path is absolute, this method will simply check if a file/folder exists at that
	 * path and, if so, return that path or <code>null</code> otherwise.
	 * <p>
	 * Note that this operation is not merely an abstract path operation, the file system will be accessed by this
	 * method (to change paths to absolute paths, check if a file/folder exists, etc.).
	 *
	 * @param document
	 *            the document.
	 * @param pathStr
	 *            the path to resolve, as a string. May be absolute or relative and may point to a file or folder. A
	 *            single file/folder name without any path information is allowed and treated as a special case of a
	 *            relative path.
	 * @return an <em>absolute</em> path to an <em>existing</em> file/folder or <code>null</code> if no existing
	 *         file/folder was found for the given path string.
	 */
	public static Path resolveFilePath(Document document, String pathStr) {
		return Iterables.getFirst(resolveFilePath(document, pathStr, false), null);
	}

	/**
	 * Same as {@link #resolveFilePath(Document, String)}, but can return all matches (iff given <code>true</code> as
	 * last argument) instead of only the first one.
	 *
	 * @return a list of <em>absolute</em> paths to <em>existing</em> files/folders. May be empty in case no matches
	 *         were found. Never returns <code>null</code>.
	 */
	public static List<Path> resolveFilePath(Document document, String pathStr, boolean returnAllMatches) {
		Objects.requireNonNull(document);
		Objects.requireNonNull(pathStr);
		final Path path = Paths.get(pathStr);
		if (path.isAbsolute()) {
			return path.toFile().exists() ? Collections.singletonList(path) : Collections.emptyList();
		}
		final List<Path> matches = new LinkedList<>();
		final Path docdir = getDocumentBasePath(document);
		Path dir = docdir.toAbsolutePath(); // need absolute, so that
											// Path#getParent() will find all
											// parents up to root
		while (dir != null && (returnAllMatches || matches.isEmpty())) {
			final Path candidate = dir.resolve(path);
			if (candidate.toFile().exists()) {
				matches.add(candidate);
			}
			dir = dir.getParent();
		}
		// at this point, we know:
		// matches.isEmpty() || for all p in matches: p.isAbsolute() &&
		// p.toFile().exists()
		return matches;
	}

	/**
	 * Returns the values of a multi-valued attribute with the given name prefix.
	 *
	 * @see #getMultiValuedAttribute(Map, String)
	 *
	 * @param node
	 *            the node whose attributes should be searched
	 * @param namePrefix
	 *            the name prefix of the multi-valued attribute
	 * @return a map containing the attribute values as specified in {@link #getMultiValuedAttribute(Map, String)}
	 */
	public static Map<String, String> getMultiValuedAttribute(ContentNode node, String namePrefix) {
		return getMultiValuedAttribute(node.getAttributes(), namePrefix);
	}

	/**
	 * Returns the values of a multi-valued attribute with the given name prefix. A multi-valued attribute consists of
	 * multiple attributes that share the same prefix. The return value maps the suffix of each of these attributes to
	 * its value. Consider the following example.
	 *
	 * Suppose that the following attributes were defined in the Asciidoc file: <code>
	 * :task_def_GH: this is the first value
	 * :task_def_IDE: this is another value
	 * :tasky_mc_taskface: what person would write this?
	 * </code>
	 *
	 * Calling this function with the string <code>"task_def_"</code> as the name prefix will return the following map.
	 *
	 * <code>
	 * "GH"  => "this is the first value",
	 * "IDE" => "this is another value"
	 * </code>
	 *
	 * Calling this function with the string <code>"task"</code> as the name prefix will return the following map.
	 *
	 * <code>
	 * "_def_GH"       => "this is the first value",
	 * "_def_IDE"      => "this is another value"
	 * "y_mc_taskface" => "what person would write this?"
	 * </code>
	 *
	 * @param attributes
	 *            the map of attributes to search
	 * @param namePrefix
	 *            the name prefix of the multi-valued attribute
	 * @return a map containing the attribute values as specified above
	 */
	public static Map<String, String> getMultiValuedAttribute(Map<String, Object> attributes, String namePrefix) {
		Map<String, String> result = new HashMap<>();
		for (Entry<String, Object> attribute : attributes.entrySet()) {
			String name = attribute.getKey();
			if (name.startsWith(namePrefix)) {
				String suffix = name.substring(namePrefix.length());
				result.put(suffix, attribute.getValue().toString());
			}
		}

		return result;
	}

	/**
	 * Returns the string value of an attribute with the given name in the given content node.
	 *
	 * @see #getAttributeAsString(Map, String, String) for further details
	 *
	 * @param node
	 *            the node
	 * @param name
	 *            the attribute name
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 */
	public static String getAttributeAsString(ContentNode node, String name, String defaultValue) {
		return getAttributeAsString(node.getAttributes(), name, defaultValue);
	}

	/**
	 * Returns the string value of an attribute with the given name in the given attribute map. If the map does not
	 * contain a value for the given name, the <code>defaultValue</code> is returned. Otherwise, the value is converted
	 * to string by means of its {@link Object#toString()} method.
	 *
	 * @param attributes
	 *            the attributes map
	 * @param name
	 *            the attribute name
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 */
	public static String getAttributeAsString(Map<String, Object> attributes, String name, String defaultValue) {
		return getAttributeAsString(defaultValue, attributes, name);
	}

	private static String getAttributeAsString(String defaultValue, Map<String, Object> attributes, String... names) {
		Object value = null;
		for (String name : names) {
			value = attributes.get(name);
			if (value != null)
				break;
		}

		if (value == null) {
			return defaultValue;
		}
		return value.toString();
	}

	/**
	 * Returns the string value of an anonymous attribute in the given attribute map. If the map does not contain a
	 * value for the given position, the <code>defaultValue</code> is returned. Otherwise, the value is converted to
	 * string by means of its {@link Object#toString()} method.<br/>
	 * <br/>
	 * This function assumes that the attribute list is from a <code>@ContentModel(ContentModel.RAW)</code> annotated
	 * {@link InlineMacroProcessor}.
	 *
	 * @param attributes
	 *            the attributes map
	 * @param pos
	 *            the attribute position
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 * @throws ParseException
	 *             when the raw text could not be parsed.
	 */
	public static String getRawAttributeAsString(Map<String, Object> attributes, int pos, String defaultValue)
			throws ParseException {
		return getRawAttributeAsString(attributes, String.valueOf(pos), defaultValue);
	}

	/**
	 * Returns the string value of an attribute with the given name in the given attribute map. If the map does not
	 * contain a value for the given name, the <code>defaultValue</code> is returned. Otherwise, the value is converted
	 * to string by means of its {@link Object#toString()} method.<br/>
	 * <br/>
	 * This function assumes that the attribute list is from a <code>@ContentModel(ContentModel.RAW)</code> annotated
	 * {@link InlineMacroProcessor}.
	 *
	 * @param attributes
	 *            the attributes map
	 * @param name
	 *            the attribute name
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 * @throws ParseException
	 *             when the raw text could not be parsed.
	 */
	public static String getRawAttributeAsString(Map<String, Object> attributes, String name, String defaultValue)
			throws ParseException {
		String text = String.valueOf(attributes.get("text"));
		attributes = AttributeParser.parse(text);
		return getAttributeAsString(attributes, name, defaultValue);
	}

	/**
	 * Returns the string value of an attribute with the given name or position in the given attribute map. If the map
	 * does not contain a value for the given name or position, the <code>defaultValue</code> is returned. Otherwise,
	 * the value is converted to string by means of its {@link Object#toString()} method.<br/>
	 * <br/>
	 * This function assumes that the attribute list is from a <code>@ContentModel(ContentModel.RAW)</code> annotated
	 * {@link InlineMacroProcessor}.
	 *
	 * @param attributes
	 *            the attributes map
	 * @param name
	 *            the attribute name
	 * @param pos
	 *            the attribute position
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 * @throws ParseException
	 *             when the raw text could not be parsed.
	 */
	public static String getRawAttributeAsString(Map<String, Object> attributes, String name, int pos,
			String defaultValue) throws ParseException {
		String text = String.valueOf(attributes.get("text"));
		Map<String, Object> textAttrs = AttributeParser.parse(text);
		return getAttributeAsString(defaultValue, textAttrs, name, String.valueOf(pos));
	}

	/**
	 * Returns the string value of an attribute with the given name in the given content node.
	 *
	 * @see #getAttributeAsString(Map, String, String) for further details
	 *
	 * @param node
	 *            the node
	 * @param name
	 *            the attribute name
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 */
	public static Integer getAttributeAsInteger(ContentNode node, String name, Integer defaultValue) {
		return getAttributeAsInteger(node.getAttributes(), name, defaultValue);
	}

	/**
	 * Returns the string value of an attribute with the given name in the given attribute map. If the map does not
	 * contain a value for the given name, the empty string is returned. Otherwise, the value is converted to string by
	 * means of its {@link Object#toString()} method.
	 *
	 * @param attributes
	 *            the attributes map
	 * @param name
	 *            the attribute name
	 * @param defaultValue
	 *            the default value to return if the given node does not contain an attribute with the given name
	 * @return the string value of the attribute
	 */
	public static Integer getAttributeAsInteger(Map<String, Object> attributes, String name, Integer defaultValue) {
		Object rawValue = attributes.get(name);
		if (rawValue == null) {
			return defaultValue;
		}
		return parseInt(rawValue.toString());
	}

	/**
	 * Parses the given string into an integer value.
	 *
	 * @see Integer#parseInt(String)
	 *
	 * @param str
	 *            the string to parse
	 * @return the integer representing the given string value, or <code>null</code> if the given string could not be
	 *         parsed
	 */
	public static Integer parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Substitutes every occurrence of the given variables with their values. To be more precise, for each entry of the
	 * given map, the given subject will be searched for <code>'${entry.getKey()}'</code>, whereby
	 * <code>entry.getKey()</code> is the actual key of the entry. For each occurrence of this search pattern in the
	 * given subject, the occurrence will be substituted with the entry's value.
	 *
	 * @param subject
	 *            the string to process
	 * @param variables
	 *            the variables and their substitutions.
	 * @return the processed string
	 */
	public static String transformVariables(String subject, Map<String, String> variables) {
		StringBuilder str = new StringBuilder(subject);

		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();

			transformVariable(str, name, value);
		}

		return str.toString();
	}

	/**
	 * Substitutes every occurrence of the given variable name with the given value. Specifically, the string
	 * <code>'${variable_name}'</code>, where <code>'variable_name'</code> is the value of the given name, will be
	 * substituted with the given value.
	 *
	 * @param subject
	 *            the string to process
	 * @param name
	 *            the variable name to substitute
	 * @param value
	 *            the variable value to substitute with
	 * @return the processed string
	 */
	public static String transformVariable(String subject, String name, String value) {
		StringBuilder str = new StringBuilder(subject);
		transformVariable(str, name, value);
		return str.toString();
	}

	private static void transformVariable(StringBuilder str, String name, String value) {
		String pattern = "{" + name + "}";
		int index = str.indexOf(pattern);
		while (index >= 0) {
			str.replace(index, index + pattern.length(), value);
			index = str.indexOf(pattern, index + value.length());
		}
	}

	/**
	 * Prints the AST starting at the given document to <code>System.out</code>.
	 *
	 * @param document
	 *            the root of the AST to print
	 */
	public static void printAST(Document document) {
		System.out.println(describeContentNode(document));
		printChildren(document.getBlocks(), "");
	}

	private static void printContentNode(ContentNode node, String indent, boolean last) {
		if (node instanceof Document) {
			printDocument((Document) node, indent, last);
		} else if (node instanceof org.asciidoctor.ast.List) {
			printList((org.asciidoctor.ast.List) node, indent, last);
		} else if (node instanceof DescriptionList) {
			printDescriptionList((DescriptionList) node, indent, last);
		} else if (node instanceof Table) {
			printTable((Table) node, indent, last);
		} else if (node instanceof StructuralNode) {
			printStructuralNode((StructuralNode) node, indent, last);
		} else {
			printTreeItem(node, indent, last);
		}
	}

	private static void printDocument(Document document, String indent, boolean last) {
		indent = printTreeItem(describeContentNode(document), indent, last);
		printChildren(document.getBlocks(), indent);
	}

	private static void printList(org.asciidoctor.ast.List list, String indent, boolean last) {
		indent = printTreeItem(describeContentNode(list), indent, last);
		printChildren(list.getItems(), indent);
	}

	private static void printDescriptionList(DescriptionList list, String indent, boolean last) {
		indent = printTreeItem(describeContentNode(list), indent, last);

		Iterator<DescriptionListEntry> iterator = list.getItems().iterator();
		while (iterator.hasNext()) {
			DescriptionListEntry item = iterator.next();
			String childIndent = printTreeItem(item, indent, !iterator.hasNext());
			printChildren(item.getTerms(), childIndent);
		}
	}

	private static void printTable(Table table, String indent, boolean last) {
		indent = printTreeItem(describeContentNode(table), indent, last);
	}

	private static void printStructuralNode(StructuralNode node, String indent, boolean last) {
		indent = printTreeItem(describeContentNode(node), indent, last);
		printChildren(node.getBlocks(), indent);
	}

	private static String describeContentNode(ContentNode node) {
		StructuralNode structuralNode = node instanceof StructuralNode ? (StructuralNode) node : null;

		StringBuilder result = new StringBuilder();
		result.append(node);
		result.append(" { ");
		result.append("name: ").append(node.getNodeName());
		result.append(", context: ").append(node.getContext());
		if (structuralNode != null) {
			result.append(", content_model: ").append(structuralNode.getContentModel());
			result.append(", level: ").append(structuralNode.getLevel());
			result.append(", title: ").append(structuralNode.getTitle());
			result.append(", style: ").append(structuralNode.getStyle());
		}
		result.append(", attributes: ").append(node.getAttributes());
		result.append(", roles: ").append(node.getRoles());
		if (structuralNode != null)
			result.append(", substitutions: ").append(structuralNode.getSubstitutions());
		result.append(" }");
		return result.toString();
	}

	private static void printChildren(Collection<? extends ContentNode> children, String indent) {
		Iterator<? extends ContentNode> iterator = children.iterator();
		while (iterator.hasNext())
			printContentNode(iterator.next(), indent, !iterator.hasNext());
	}

	private static String printTreeItem(Object o, String indent, boolean last) {
		System.out.println(indent + (last ? "└╴" : "├╴") + o);
		return indent + (last ? "  " : "│ ");
	}
}
