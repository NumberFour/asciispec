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
			:repolnk_def_n4js: https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/{FILE};images/confluence.png
			
		'''

	@Test
	public def void testMissingConfiguration() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'n4js']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			This is some inline text repolnk:n4js:docs/pom.xml[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 1: Missing repolnk configuration for repo: 'n4js'"
		);
	}

	@Test
	public def void testInvalidConfiguration1() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p><mark>[Error: Invalid repolnk configuration: :repolnk_def_n4js: <a href="https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/{FILE}" class="bare">https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/{FILE}</a>]</mark></p>
			</div>
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'n4js']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_n4js: https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/{FILE}
			
			This is some inline text repolnk:n4js:docs/pom.xml[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Invalid repolnk configuration: :repolnk_def_n4js: https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/{FILE}"
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
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'n4js']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_n4js: https://github.numberfour.eu/NumberFour/n4js/blob/{FILE};
			
			This is some inline text repolnk:n4js:docs/pom.xml[] and so on.

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
			<p>This is some inline text <mark>[Error: Missing repolnk configuration for repo: 'n4js']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			:linkattrs:
			:repolnk_def_n4js: https://github.numberfour.eu/NumberFour/n4js/blob/{BRANCH}/docs/;
			
			This is some inline text repolnk:n4js:docs/pom.xml[] and so on.

			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Invalid repolnk configuration in URL: FILE placeholder missing"
		);
	}

	@Test
	public def void testArticleLinkWithoutTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/BR/Home"><img src="images/confluence.png" alt="BR/Home" title="BR/Home"></a></span><a href="https://confluence.numberfour.eu/display/BR/Home" title="BR/Home">Confluence entry: BR/Home</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text cwiki:BR/Home[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testPageLinkWithoutTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <span class="image"><a class="image" href="https://confluence.numberfour.eu/pages/viewpage.action?pageId=1234545"><img src="images/confluence.png" alt="1234545" title="1234545"></a></span><a href="https://confluence.numberfour.eu/pages/viewpage.action?pageId=1234545" title="1234545">Confluence entry: 1234545</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»

			This is some inline text cwiki:1234545[] and so on.

			More lines for good measure.'''
		);
	}

	@Test
	public def void testArticleLinkWithExplicitTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some other text <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/BR/Home"><img src="images/confluence.png" alt="A good read" title="A good read"></a></span><a href="https://confluence.numberfour.eu/display/BR/Home" title="A good read">Confluence entry: A good read</a> and more blablah.</p>
			</div>''',
			'''
			«config»
			Some other text cwiki:BR/Home[title="A good read"] and more blablah.'''
		);
	}

	@Test
	public def void testArticleLinkWithAnonymousTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some other text <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/BR/Home"><img src="images/confluence.png" alt="A good read" title="A good read"></a></span><a href="https://confluence.numberfour.eu/display/BR/Home" title="A good read">Confluence entry: A good read</a> and more blablah.</p>
			</div>''',
			'''
			«config»
			Some other text cwiki:BR/Home[A good read] and more blablah.'''
		);
	}

	@Test
	public def void testArticleLinkWithMultiAttributeTitle() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some other text <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/BR/Home"><img src="images/confluence.png" alt="A good read" title="A good read"></a></span><a href="https://confluence.numberfour.eu/display/BR/Home" title="A good read">Confluence entry: A good read</a> and more blablah.</p>
			</div>''',
			'''
			«config»
			Some other text cwiki:BR/Home["A good read", Test] and more blablah.'''
		);
	}

	@Test
	public def void testNestedSquareBrackets() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Some other text <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/BR/Home"><img src="images/confluence.png" alt="AAA <code>code</code> AAA" title="AAA <code>code</code> AAA"></a></span><a href="https://confluence.numberfour.eu/display/BR/Home" title="AAA <code class=">Confluence entry: AAA <code>code</code> AAA</a> and more blablah.</p>
			</div>''',
			'''
			«config»
			Some other text cwiki:BR/Home["AAA [.N4JS]`code` AAA"] and more blablah.'''
		);
	}

}