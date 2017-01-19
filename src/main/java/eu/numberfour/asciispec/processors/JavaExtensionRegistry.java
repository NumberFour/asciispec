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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;

/**
 * Reimplementation of {@link org.asciidoctor.extension.JavaExtensionRegistry} that also handles inline macros that have
 * their output appended to a new block.
 *
 * @see InlineMacroToBlockConverter
 * @see org.asciidoctor.extension.JavaExtensionRegistry
 */
public class JavaExtensionRegistry {

	private final org.asciidoctor.extension.JavaExtensionRegistry delegateRegistry;

	private final Map<String, InlineMacroToBlockConverter> converters;

	/**
	 * Creates a new instance that delegates all calls to the given extension registry.
	 *
	 * @param delegateRegistry
	 *            the registry to delegate to
	 */
	public JavaExtensionRegistry(org.asciidoctor.extension.JavaExtensionRegistry delegateRegistry) {
		this.delegateRegistry = Objects.requireNonNull(delegateRegistry);
		this.converters = new HashMap<>();
	}

	/**
	 * Registers the given inline macro processor under the given macro name with an inline to block converter with the
	 * given sibling block context.
	 *
	 * @param siblingContext
	 *            the context name for the sibling block that is to be created by the converter
	 * @param macroName
	 *            the name of the inline macro to process
	 * @param inlineMacroProcessor
	 *            the processor for the inline macro
	 */
	public void inlineMacroToSiblingBlock(String siblingContext, String macroName,
			InlineMacroProcessor inlineMacroProcessor) {
		InlineMacroToBlockConverter converter = getConverter(Objects.requireNonNull(siblingContext));
		converter.registerInlineMacroProcessor(macroName, inlineMacroProcessor);
	}

	/**
	 * Registers the given inline macro processor with an inline to block converter with the given sibling block
	 * context. The macro name is obtained from the given processor using {@link InlineMacroProcessor#getName()}.
	 *
	 * @param siblingContext
	 *            the context name for the sibling block that is to be created by the converter
	 * @param inlineMacroProcessor
	 *            the processor for the inline macro
	 */
	public void inlineMacroToSiblingBlock(String siblingContext, InlineMacroProcessor inlineMacroProcessor) {
		inlineMacroToSiblingBlock(siblingContext, inlineMacroProcessor.getName(), inlineMacroProcessor);
	}

	/**
	 * Registers an instance of the given inline macro processor class under the given macro name with an inline to
	 * block converter with the given sibling block context.
	 *
	 * @param siblingContext
	 *            the context name for the sibling block that is to be created by the converter
	 * @param macroName
	 *            the name of the inline macro to process
	 * @param inlineMacroProcessor
	 *            the processor class for the inline macro
	 */
	public void inlineMacroToSiblingBlock(String siblingContext, String macroName,
			Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
		try {
			inlineMacroToSiblingBlock(siblingContext, macroName, inlineMacroProcessor.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers an instance of the given inline macro processor class with an inline to block converter with the given
	 * sibling block context. The macro name is obtained from the given processor using
	 * {@link InlineMacroProcessor#getName()}.
	 *
	 * @param siblingContext
	 *            the context name for the sibling block that is to be created by the converter
	 * @param inlineMacroProcessor
	 *            the processor class for the inline macro
	 */
	public void inlineMacroToSiblingBlock(String siblingContext,
			Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
		try {
			inlineMacroToSiblingBlock(siblingContext, inlineMacroProcessor.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private InlineMacroToBlockConverter getConverter(String siblingContext) {
		InlineMacroToBlockConverter result = converters.get(siblingContext);
		if (result == null) {
			result = new InlineMacroToBlockConverter(siblingContext);
			converters.put(siblingContext, result);
			treeprocessor(result);
		}
		return result;
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#docinfoProcessor(Class)
	 */
	public void docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
		delegateRegistry.docinfoProcessor(docInfoProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#docinfoProcessor(DocinfoProcessor)
	 */
	public void docinfoProcessor(DocinfoProcessor docInfoProcessor) {
		delegateRegistry.docinfoProcessor(docInfoProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#docinfoProcessor(String)
	 */
	public void docinfoProcessor(String docInfoProcessor) {
		delegateRegistry.docinfoProcessor(docInfoProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#preprocessor(Class)
	 */
	public void preprocessor(Class<? extends Preprocessor> preprocessor) {
		delegateRegistry.preprocessor(preprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#preprocessor(Preprocessor)
	 */
	public void preprocessor(Preprocessor preprocessor) {
		delegateRegistry.preprocessor(preprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#preprocessor(String)
	 */
	public void preprocessor(String preprocessor) {
		delegateRegistry.preprocessor(preprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#postprocessor(String)
	 */
	public void postprocessor(String postprocessor) {
		delegateRegistry.postprocessor(postprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#postprocessor(Class)
	 */
	public void postprocessor(Class<? extends Postprocessor> postprocessor) {
		delegateRegistry.postprocessor(postprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#postprocessor(Postprocessor)
	 */
	public void postprocessor(Postprocessor postprocessor) {
		delegateRegistry.postprocessor(postprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#includeProcessor(String)
	 */
	public void includeProcessor(String includeProcessor) {
		delegateRegistry.includeProcessor(includeProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#includeProcessor(Class)
	 */
	public void includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
		delegateRegistry.includeProcessor(includeProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#includeProcessor(IncludeProcessor)
	 */
	public void includeProcessor(IncludeProcessor includeProcessor) {
		delegateRegistry.includeProcessor(includeProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#treeprocessor(Treeprocessor)
	 */
	public void treeprocessor(Treeprocessor treeprocessor) {
		delegateRegistry.treeprocessor(treeprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#treeprocessor(Class)
	 */
	public void treeprocessor(Class<? extends Treeprocessor> treeprocessor) {
		delegateRegistry.treeprocessor(treeprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#treeprocessor(String)
	 */
	public void treeprocessor(String treeprocessor) {
		delegateRegistry.treeprocessor(treeprocessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(String, String)
	 */
	public void block(String blockName, String blockProcessor) {
		delegateRegistry.block(blockName, blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(String)
	 */
	public void block(String blockProcessor) {
		delegateRegistry.block(blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(String, Class)
	 */
	public void block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
		delegateRegistry.block(blockName, blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(Class)
	 */
	public void block(Class<? extends BlockProcessor> blockProcessor) {
		delegateRegistry.block(blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(BlockProcessor)
	 */
	public void block(BlockProcessor blockProcessor) {
		delegateRegistry.block(blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#block(String, BlockProcessor)
	 */
	public void block(String blockName, BlockProcessor blockProcessor) {
		delegateRegistry.block(blockName, blockProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#blockMacro(String, Class)
	 */
	public void blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
		delegateRegistry.blockMacro(blockName, blockMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#blockMacro(Class)
	 */
	public void blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
		delegateRegistry.blockMacro(blockMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#blockMacro(String, String)
	 */
	public void blockMacro(String blockName, String blockMacroProcessor) {
		delegateRegistry.blockMacro(blockName, blockMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#blockMacro(String)
	 */
	public void blockMacro(String blockMacroProcessor) {
		delegateRegistry.blockMacro(blockMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#blockMacro(BlockMacroProcessor)
	 */
	public void blockMacro(BlockMacroProcessor blockMacroProcessor) {
		delegateRegistry.blockMacro(blockMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#inlineMacro(InlineMacroProcessor)
	 */
	public void inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
		delegateRegistry.inlineMacro(inlineMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#inlineMacro(String, Class)
	 */
	public void inlineMacro(String blockName, Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
		delegateRegistry.inlineMacro(blockName, inlineMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#inlineMacro(Class)
	 */
	public void inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
		delegateRegistry.inlineMacro(inlineMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#inlineMacro(String, String)
	 */
	public void inlineMacro(String blockName, String inlineMacroProcessor) {
		delegateRegistry.inlineMacro(blockName, inlineMacroProcessor);
	}

	/**
	 * @see org.asciidoctor.extension.JavaExtensionRegistry#inlineMacro(String)
	 */
	public void inlineMacro(String inlineMacroProcessor) {
		delegateRegistry.inlineMacro(inlineMacroProcessor);
	}
}
