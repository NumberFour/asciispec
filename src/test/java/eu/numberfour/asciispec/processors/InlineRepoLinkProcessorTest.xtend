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
 * Test cases for {@link InlineWikiLinkProcessor}.
 */
class InlineRepoLinkProcessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		new InlineRepoLinkExtension().register(doc);
	}

	private def getConfig() '''
			:linkattrs:
			:repolnk_def_aspec: https://github.com/NumberFour/asciispec/blob/{BRANCH}/{FILE};images/icons/github.png
			
		'''

	@Test
	public def void testMissingConfiguration() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'aspec']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			This is some inline text repolnk:aspec:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 1: Missing repolnk configuration for repo: 'aspec'"
		);
	}

	@Test
	public def void testInvalidConfiguration1() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid repolnk configuration: :repolnk_def_aspec: <a href="https://github.com/NumberFour/asciispec/blob/{BRANCH}/{FILE}" class="bare">https://github.com/NumberFour/asciispec/blob/{BRANCH}/{FILE}</a>]</mark></p>
			</div>
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'aspec']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_aspec: https://github.com/NumberFour/asciispec/blob/{BRANCH}/{FILE}
			
			This is some inline text repolnk:aspec:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Invalid repolnk configuration: :repolnk_def_aspec: https://github.com/NumberFour/asciispec/blob/{BRANCH}/{FILE}"
		);
	}

	@Test
	public def void testInvalidConfigurationMissingBranch() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid repolnk configuration in URL: BRANCH placeholder missing]</mark></p>
			</div>
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'aspec']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_aspec: https://github.com/NumberFour/asciispec/blob/{FILE};
			
			This is some inline text repolnk:aspec:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Invalid repolnk configuration in URL: BRANCH placeholder missing"
		);
	}

	@Test
	public def void testInvalidConfigurationMissingFile() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid repolnk configuration in URL: FILE placeholder missing]</mark></p>
			</div>
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'aspec']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_aspec: https://github.com/NumberFour/asciispec/blob/{BRANCH}/docs/;
			
			This is some inline text repolnk:aspec:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Invalid repolnk configuration in URL: FILE placeholder missing"
		);
	}

	@Test
	public def void testRepoLink() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/master/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="docs/custom-processors/inline-cwiki-macro.adoc" title="docs/custom-processors/inline-cwiki-macro.adoc"></a></span><a href="https://github.com/NumberFour/asciispec/blob/master/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/master/docs/custom-processors/inline-cwiki-macro.adoc">docs/custom-processors/inline-cwiki-macro.adoc</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text repolnk:aspec:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testRepoLinkWithBranch() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="docs/custom-processors/inline-cwiki-macro.adoc" title="docs/custom-processors/inline-cwiki-macro.adoc"></a></span><a href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc">docs/custom-processors/inline-cwiki-macro.adoc</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text repolnk:aspec:AS-1:docs/custom-processors/inline-cwiki-macro.adoc[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testRepoLinkWithCommit() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/06ebaaa29954b2b0f4deeb6188804badd7e4ffea/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend"><img src="images/icons/github.png" alt="src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend" title="src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend"></a></span><a href="https://github.com/NumberFour/asciispec/blob/06ebaaa29954b2b0f4deeb6188804badd7e4ffea/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend" title="https://github.com/NumberFour/asciispec/blob/06ebaaa29954b2b0f4deeb6188804badd7e4ffea/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend">src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text repolnk:aspec:06ebaaa29954b2b0f4deeb6188804badd7e4ffea:src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testRepoLinkWithCommitShorthand() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/06ebaaa/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend"><img src="images/icons/github.png" alt="src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend" title="src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend"></a></span><a href="https://github.com/NumberFour/asciispec/blob/06ebaaa/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend" title="https://github.com/NumberFour/asciispec/blob/06ebaaa/src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend">src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text repolnk:aspec:06ebaaa:src/test/java/eu/numberfour/asciispec/processors/InlineRepoLinkProcessorTest.xtend[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testRepoLinkWithExplicitTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="inline-cwiki-macro.adoc" title="inline-cwiki-macro.adoc"></a></span><a href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc">inline-cwiki-macro.adoc</a> and so on.</p>
			</div>''',
			'''
			«config»
			This is some inline text repolnk:aspec:AS-1:docs/custom-processors/inline-cwiki-macro.adoc[title=inline-cwiki-macro.adoc] and so on.'''
		);
	}

	@Test
	public def void testRepoLinkWithAnonymousTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="inline-cwiki-macro.adoc" title="inline-cwiki-macro.adoc"></a></span><a href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc">inline-cwiki-macro.adoc</a> and so on.</p>
			</div>''',
			'''
			«config»
			This is some inline text repolnk:aspec:AS-1:docs/custom-processors/inline-cwiki-macro.adoc[inline-cwiki-macro.adoc] and so on.'''
		);
	}

	@Test
	public def void testRepoLinkWithMultiAttributeTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="inline-cwiki-macro.adoc" title="inline-cwiki-macro.adoc"></a></span><a href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc">inline-cwiki-macro.adoc</a> and so on.</p>
			</div>''',
			'''
			«config»
			This is some inline text repolnk:aspec:AS-1:docs/custom-processors/inline-cwiki-macro.adoc["inline-cwiki-macro.adoc", Test] and so on.'''
		);
	}

	@Test
	public def void testRepoLinkWithNestedSquareBrackets() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some other text <span class="image"><a class="image" href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc"><img src="images/icons/github.png" alt="AAA <code>code</code> AAA" title="AAA <code>code</code> AAA"></a></span><a href="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc" title="https://github.com/NumberFour/asciispec/blob/AS-1/docs/custom-processors/inline-cwiki-macro.adoc">AAA <code>code</code> AAA</a> and more blablah.</p>
			</div>''',
			'''
			«config»
			Some other text repolnk:aspec:AS-1:docs/custom-processors/inline-cwiki-macro.adoc["AAA [.N4JS]`code` AAA"] and more blablah.'''
		);
	}

}