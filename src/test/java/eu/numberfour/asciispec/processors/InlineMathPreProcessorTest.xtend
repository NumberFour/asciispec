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
import org.junit.Ignore

/**
 * Test cases for {@link InlineMathPreprocessor}.
 */
class InlineMathPreProcessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new InlineMathExtension().register(doc);
	}

	@Test
	public def void testEmptyExpression() {
		convertAndAssert(
			'''<simpara>some basic  math</simpara>''',
			'''
			some basic math:[] math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMathInCommentBlock() {
		convertAndAssert(
			'''''',
			'''
			////
			$e=mc^2$
			////
			''',
			Backend.DOCBOOK
		);
	}

	// TODO: AS-51 see https://github.com/NumberFour/asciispec/issues/51
	@Ignore
	@Test
	public def void testMathInCodeBlock() {
		convertAndAssert(
			'''''',
			'''
			----
			$e=mc^2$
			----
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testEmptyShorthandExpression() {
		// must not be picked up because it may be used as a math block delimiter
		convertAndAssert(
			'''<simpara>some basic $$ math</simpara>''',
			'''
			some basic $$ math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandInlineExpression() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><msubsup><mo>⨄</mo><mi>a</mi><mi>b</mi></msubsup><msub><mi>A</mi><mi>λ</mi></msub></math> math</simpara>''',
			'''
			Some more $\biguplus_a^b A_{\lambda}$ math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testExpressionWithInlineDollarDelimiters() {
		convertStringAndAssertErrorContains(
			'''
			<simpara>Some more <emphasis role="marked">[Error: Unable to parse inline math expression '\biguplus_a^b $ A_{\lambda}': Must not contain math delimiter '$']</emphasis> math</simpara>''',
			'''
			Some more math:\biguplus_a^b $ A_{\lambda}[] math
			''',
			"asciispec  : ERROR: line 1: Unable to parse inline math expression '\\biguplus_a^b $ A_{\\lambda}': Must not contain math delimiter '$'",
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testExpressionWithoutDollarDelimiters() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><msubsup><mo>⨄</mo><mi>a</mi><mi>b</mi></msubsup><msub><mi>A</mi><mi>λ</mi></msub></math> math</simpara>''',
			'''
			Some more math:\biguplus_a^b A_{\lambda}[] math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMultipleShorthandExpressions() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math>, how great.</simpara>
			<simpara>But here&#8217;s even more: <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi><mo>+</mo><mi>y</mi><mo>=</mo><mi>z</mi></math>.</simpara>''',
			'''
			Some more $x$ math $y$, how great.

			But here's even more: $x+y=z$.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandExpressionAtBeginningOfLine() {
		convertAndAssert(
			'''
			<simpara><math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> is math, too.</simpara>''',
			'''
			$x$ is math, too.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandExpressionEscapeWithPass() {
		convertAndAssert(
			'''
			<simpara>We really do not want $this to be math $, so we escape it with inline passing!</simpara>
			<simpara>What about a single $ in a line? Should not be a problem.</simpara>''',
			'''
			We really do not want +++$+++this to be math +++$+++, so we escape it with inline passing!

			What about a single $ in a line? Should not be a problem.''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandExpressionEscapeWithBackslash() {
		convertAndAssert(
			'''
			<simpara>We really do not want $this to be math $, so we escape it with backslashes!</simpara>
			<simpara>What about a single $ in a line? Should not be a problem.</simpara>''',
			'''
			We really do not want \$this to be math \$, so we escape it with backslashes!

			What about a single $ in a line? Should not be a problem.''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testConfigValueWithEscapedDollars() {
		convertAndAssert(
			'''
			<simpara>Here is the value: GitHub;N4JS-N4 Issues;some_text/${TASK_ID};images/github.png;GH-${TASK_ID} !!!</simpara>''',
			'''
			:A_VALUE: GitHub;N4JS-N4 Issues;some_text/\${TASK_ID};images/github.png;GH-\${TASK_ID}

			Here is the value: {A_VALUE} !!!''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandExpressionWithEscapedBackslash() {
		// It's debatable that this should work, since the backslash is itself escaped. But this would add extra
		// complexity to the macro processor, and the same effect can be achieved by using inline pass syntax, so it's
		// not strictly necessary.
		convertAndAssert(
			'''
			<simpara>But \$x$ should be math!</simpara>''',
			'''
			But \\$x$ should be math!''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testShorthandWithMathInlineExpression() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><msubsup><mo>⨄</mo><mi>a</mi><mi>b</mi></msubsup><msub><mi>A</mi><mi>λ</mi></msub></math> math</simpara>''',
			'''
			Some more math:$\biguplus_a^b A_{\lambda}$[] math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testIgnoreShorthandMathBlock() {
		// Asciidoctor removes the $$ signs.
		convertAndAssert(
			'''
			<simpara>Some more x math</simpara>''',
			'''
			Some more $$x$$ math
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMixedExpressions1() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math>, how great.</simpara>''',
			'''
			Some more $x$ math math:y[], how great.
			''',
			Backend.DOCBOOK
		);
	}

	public def void testMixedExpressions2() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math>, how great.</simpara>''',
			'''
			Some more math:y[] math $x$, how great.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMixedExpressions3() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math> math, how great and that weird one <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math></simpara>''',
			'''
			Some more math:y[] math, how great and that weird one math:$x$[]
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testMixedExpressions4() {
		convertAndAssert(
			'''
			<simpara>AAA <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math> BBB <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> CCC <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> DDD <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math></simpara>''',
			'''
			AAA math:y[] BBB math:$x$[] CCC $x$ DDD math:$y$[]
			''',
			Backend.DOCBOOK
		);
	}


	@Test
	public def void testSimilarExpressionsInOneLine1() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math>, how great.</simpara>''',
			'''
			Some more $x$ math $y$, how great.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testSimilarExpressionsInOneLine2() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math>, how great.</simpara>''',
			'''
			Some more math:x[] math math:y[], how great.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testSimilarExpressionsInOneLine3() {
		convertAndAssert(
			'''
			<simpara>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math> math <math xmlns="http://www.w3.org/1998/Math/MathML"><mi>y</mi></math>, how great.</simpara>''',
			'''
			Some more math:$x$[] math math:$y$[], how great.
			''',
			Backend.DOCBOOK
		);
	}

	@Test
	public def void testHTMLOutput() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some more <math xmlns="http://www.w3.org/1998/Math/MathML"><msubsup><mo>⨄</mo><mi>a</mi><mi>b</mi></msubsup><msub><mi>A</mi><mi>λ</mi></msub></math> math</p>
			</div>''',
			'''
			Some more $\biguplus_a^b A_{\lambda}$ math
			''',
			Backend.HTML5
		);
	}
}