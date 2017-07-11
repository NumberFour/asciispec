/**
 * Copyright (c) 2017 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package eu.numberfour.asciispec.processors

import eu.numberfour.asciispec.AsciidoctorTest
import org.junit.Before
import org.junit.Test
import eu.numberfour.asciispec.SourceProcessor

/**
 * Tests math blocks nested within other blocks.
 */
class SourceProcessorIgnoreBlocksTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		// do!
		new MathIncludeExtension().register(doc);
		// not!
		new InlineMathExtension().register(doc);
		// change!
		new MathBlockExtension().register(doc);
		// any!
		new DefinitionBlockExtension().register(doc);
		// of!
		new RequirementBlockExtension().register(doc);
		// these!
	}


	/**
	 * The processors above cause the {@link SourceProcessor} to parse each
	 * line twice. In case the {@link SourceProcessor} ignores some blocks
	 * like source blocks in the document correctly, only the second math
	 * expression will be converted. 
	 */
	@Test
	public def void testMathInSourceCodeBlock() {
		convertAndAssert(
			'''
			<programlisting role="small" language="bash" linenumbering="unnumbered">$e=mc^2$</programlisting>
			<simpara><math xmlns="http://www.w3.org/1998/Math/MathML"><mi>e</mi><mo>=</mo><mi>m</mi><msup><mi>c</mi><mn>2</mn></msup></math></simpara>''',
			'''
			[source,bash,role=small]
			----
			$e=mc^2$
			----
			$e=mc^2$
			''',
			Backend.DOCBOOK
		);
	}
}