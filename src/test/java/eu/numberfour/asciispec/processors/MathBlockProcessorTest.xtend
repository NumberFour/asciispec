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

/**
 * Test cases for {@link MathBlockProcessor}.
 */
class MathBlockProcessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new MathBlockExtension().register(doc);
	}

	@Test
	public def void testEmptyBodyNoTitle() {
		convertAndAssert(
			'''
			''',
			'''
			[math]
			++++
			++++
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testEmptyBodyWithTitle() {
		convertAndAssert(
			'''<simpara xml:id="amazing_math"><link linkend="amazing_math">Amazing Math</link></simpara>''',
			'''
			.Amazing Math
			[math]
			++++
			++++
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMathContentAndTitle() {
		convertAndAssert(
			'''
			<simpara xml:id="some_mathematics"><link linkend="some_mathematics">Some Mathematics</link></simpara>
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mn>2</mn><mfenced close=")" open="("><mrow><mi>x</mi><mo>-</mo><mn>1</mn></mrow></mfenced><mo>=</mo><mn>2</mn><mfenced close=")" open="("><mrow><mi>x</mi><mo>-</mo><mn>1</mn></mrow></mfenced></math>
			<simpara>More Content Here</simpara>''',
			'''
			.Some Mathematics
			[math]
			++++
			2(x-1) = 2(x-1)
			++++
			More Content Here
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testIndexOutOfBounds() {
		/* see https://github.numberfour.eu/NumberFour/asciispec/issues/84
		 * The given math block contains a syntax error (note the missing closing brace) that is not handled
		 * gracefully by SnuggleTex. Instead, it throws an exception which we catch and print out in an error message.
		 */
		convertAndAssert(
			'''
			<div class="openblock">
			<div class="title">Some Mathematics</div>
			<div class="content">
			<div id="some_mathematics" class="paragraph">
			<p><a href="#some_mathematics">Some Mathematics</a></p>
			</div>
			<div class="listingblock">
			<div class="content">
			<pre class="highlight"><code>&lt;: \lstnfjs{constructor\{_T_\}</code></pre>
			</div>
			</div>
			<div class="openblock issues">
			<div class="content">
			<div class="paragraph">
			<p><strong>Issues:</strong></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Math syntax error [At '\lstnfjs': TTEC00 - Undefined command {0}]</p>
			</li>
			</ul>
			</div>
			</div>
			</div>
			</div>
			</div>''',
			'''
			.Some Mathematics
			[math]
			++++
			<: \lstnfjs{constructor\{_T_\}
			++++
			''',
			Backend.HTML5
		);
	}

	@Test
	def void testInlineMathInTextModeInMathBlock() {
		convertAndAssert(
			'''
			<simpara xml:id="time_for_some_thrilling_mathematics"><link linkend="time_for_some_thrilling_mathematics">Time For Some Thrilling Mathematics</link></simpara>
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi>y</mi><mo>+</mo><mn>1</mn><mrow><mspace width="1ex"/><mtext>well if this</mtext><mspace width="1ex"/><mrow><mi>x</mi><mo>+</mo><mn>1</mn></mrow><mspace width="1ex"/><mtext>ain’t shiny!</mtext></mrow></math>''',
			'''
			.Time For Some Thrilling Mathematics
			[math]
			++++
			x+1 = y+1 \mbox{ well if this $x+1$ ain't shiny!}
			++++
			''',
			Backend.DOCBOOK
		)
	}
}