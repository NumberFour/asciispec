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
package eu.numberfour.asciispec.processors

import eu.numberfour.asciispec.AsciidoctorTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Test cases for {@link MathIncludePreprocessor}.
 */
class MathIncludePreprocessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new MathIncludeExtension().register(doc);
		new InlineMathExtension().register(doc);
		new MathBlockExtension().register(doc);
	}

	@Test
	def void testEmptyTarget() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid use of mathinclude macro: Missing or blank target]</mark></p>
			</div>''',
			'''

			mathinclude::[]

			''',
			"Invalid use of mathinclude macro: Missing or blank target"
		);
	}

	@Test
	def void testBlankTarget() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid use of mathinclude macro: Missing or blank target]</mark></p>
			</div>''',
			'''
			mathinclude::[]
			''',
			"Invalid use of mathinclude macro: Missing or blank target"
		);
	}

	@Test
	def void testMissingFile() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Unable to load LaTeX commands from 'missing/file/yup': File 'missing/file/yup' could not be found]</mark></p>
			</div>''',
			'''
			mathinclude::missing/file/yup[]
			''',
			"Unable to load LaTeX commands from 'missing/file/yup': File 'missing/file/yup' could not be found"
		);
	}

	@Test
	def void testMalformedFile() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Unable to load LaTeX commands from 'src/test/resources/data/latex_commands/malformed.tex': File commands could not be parsed.]</mark></p>
			</div>''',
			'''
			mathinclude::src/test/resources/data/latex_commands/malformed.tex[]
			''',
			"asciispec  : ERROR: line 1: Unable to load LaTeX commands from 'src/test/resources/data/latex_commands/malformed.tex': File commands could not be parsed."
		);
	}

	@Test
	def void testValidFileWithMathBlock() {
		convertAndAssert(
			'''
			<simpara xml:id="some_mathematics"><link linkend="some_mathematics">Some Mathematics</link></simpara>
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>Γ</mi><mo>⊢</mo><mfenced close=")" open="("><mi>x</mi></mfenced></math>''',
			'''
			mathinclude::src/test/resources/data/latex_commands/commands.tex[]

			.Some Mathematics
			[math]
			++++
			\tee(x)
			++++
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	def void testValidFileWithInlineMath() {
		convertAndAssert(
			'''
			<simpara>Should work inline also: <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>Γ</mi><mo>⊢</mo><mfenced close=")" open="("><mi>x</mi></mfenced></math>, but does it really?</simpara>''',
			'''
			mathinclude::src/test/resources/data/latex_commands/commands.tex[]

			Should work inline also: $\tee(x)$, but does it really?
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	def void testConvertFile() throws IOException {
		convertFileAndAssert('''
		<div class="sect1">
		<h2 id="_this_is_the_first_chapter">This is the first chapter!</h2>
		<div class="sectionbody">
		<div class="paragraph">
		<p>Let&#8217;s put some inline math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>Γ</mi><mo>⊢</mo><mfenced close=")" open="("><mi>x</mi></mfenced></math> that uses the included commands.</p>
		</div>
		</div>
		</div>''', "src/test/resources/data/latex_commands", "test_file.adoc");
	}
}