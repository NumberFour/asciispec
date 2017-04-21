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

/**
 * Tests math blocks nested within other blocks.
 */
class NestedMathBlockProcessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new MathBlockExtension().register(doc);
		new DefinitionBlockExtension().register(doc);
		new RequirementBlockExtension().register(doc);
	}

	@Test
	public def void testMathWithinRequirementBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>11:15, restate my assumptions:</p>
			</div>
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">1: Mathematics is the language of nature</a> (ver. 1)</p>
			</div>
			<div class="paragraph">
			<p>2: Everything around us can be represented and understood through numbers.</p>
			</div>
			<div class="openblock">
			<div class="content">
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>+</mo><mi>y</mi><mo>=</mo><mi>z</mi></math>
			</div>
			</div>
			<div class="paragraph">
			<p>3: If you graph these numbers, patterns emerge.</p>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>Therefore: There are patterns everywhere in nature.</p>
			</div>''',
			'''
			11:15, restate my assumptions:


			.1: Mathematics is the language of nature
			[req,id=RSL-2,version=1]
			--
			2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.
			--

			Therefore: There are patterns everywhere in nature.
			''',
			Backend.HTML5
		);
	}

		@Test
	public def void testMathWithinDefinitionBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>11:15, restate my assumptions:</p>
			</div>
			<div class="openblock definition">
			<div class="content">
			<div class="paragraph">
			<p>[[1<em>mathematics_is_the_language_of_nature]]<strong>Definition:</strong> <a href="#1</em>mathematics_is_the_language_of_nature">1: Mathematics is the language of nature</a></p>
			</div>
			<div class="paragraph">
			<p>2: Everything around us can be represented and understood through numbers.</p>
			</div>
			<div class="openblock">
			<div class="content">
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>+</mo><mi>y</mi><mo>=</mo><mi>z</mi></math>
			</div>
			</div>
			<div class="paragraph">
			<p>3: If you graph these numbers, patterns emerge.</p>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>Therefore: There are patterns everywhere in nature.</p>
			</div>''',
			'''
			11:15, restate my assumptions:


			.1: Mathematics is the language of nature
			[def]
			--
			2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.
			--

			Therefore: There are patterns everywhere in nature.
			''',
			Backend.HTML5
		);
	}

	@Test
	public def void testMathIgnoringTodoBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>11:15, restate my assumptions:</p>
			</div>
			<div class="listingblock todo">
			<div class="title">1: Mathematics is the language of nature</div>
			<div class="content">
			<pre>2: Everything around us can be represented and understood through numbers.

			$x+y=z$

			3: If you graph these numbers, patterns emerge.</pre>
			</div>
			</div>
			<div class="paragraph">
			<p>Therefore: There are patterns everywhere in nature.</p>
			</div>''',
			'''
			11:15, restate my assumptions:


			.1: Mathematics is the language of nature
			[.todo]
			----
			2: Everything around us can be represented and understood through numbers.

			$x+y=z$

			3: If you graph these numbers, patterns emerge.
			----

			Therefore: There are patterns everywhere in nature.
			''',
			Backend.HTML5
		);
	}

	@Test
	public def void testMathWithinTodoBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>11:15, restate my assumptions:</p>
			</div>
			<div class="listingblock todo">
			<div class="title">1: Mathematics is the language of nature</div>
			<div class="content">
			<pre>2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.</pre>
			</div>
			</div>
			<div class="paragraph">
			<p>Therefore: There are patterns everywhere in nature.</p>
			</div>''',
			'''
			11:15, restate my assumptions:


			.1: Mathematics is the language of nature
			[.todo]
			----
			2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.
			----

			Therefore: There are patterns everywhere in nature.
			''',
			Backend.HTML5
		);
	}

	@Test
	public def void testMathWithinSourceBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>11:15, restate my assumptions:</p>
			</div>
			<div class="listingblock">
			<div class="title">1: Mathematics is the language of nature</div>
			<div class="content">
			<pre class="highlight"><code class="language-n4js" data-lang="n4js">2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.</code></pre>
			</div>
			</div>
			<div class="paragraph">
			<p>Therefore: There are patterns everywhere in nature.</p>
			</div>''',
			'''
			11:15, restate my assumptions:


			.1: Mathematics is the language of nature
			[source,n4js]
			----
			2: Everything around us can be represented and understood through numbers.

			[math]
			++++
			x+y=z
			++++

			3: If you graph these numbers, patterns emerge.
			----

			Therefore: There are patterns everywhere in nature.
			''',
			Backend.HTML5
		);
	}
}