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
 * Test cases for {@link CitationProcessor}.
 */
class CitationProcessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		new CitationExtension().register(doc);
	}

	private def String getConfig() ''':bib-file: src/test/resources/data/citation/example.bib'''

	@Test
	public def void testOneUnknownCitationWithBibliography() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some text! [<a href="#ASDF1999">ASDF1999</a>] Yup, cited those guys. They&#8217;ll be happy now!</p>
			</div>
			<div class="openblock bibliography">
			<div class="content">
			<div class="paragraph">
			<p>Unknown reference: ASDF1999[]</p>
			</div>
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
			<p>Unknown reference: ASDF1999[]</p>
			</li>
			</ul>
			</div>
			</div>
			</div>''',
			'''
			«config»
			This is some text! cite:[ASDF1999] Yup, cited those guys. They'll be happy now!

			bibliography::[]

			''',
			"asciispec  : WARN: Unknown reference: ASDF1999[]");
	}

	@Test
	public def void testOneCitationWithBibliography() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some text! [<a href="#OMG09a">OMG09a(p.1, p.2)</a>] Yup, cited those guys. They&#8217;ll be happy now!</p>
			</div>
			<div class="openblock bibliography">
			<div class="content">
			<div class="paragraph">
			<p><a id="OMG09a"></a>OMG. (2009). <em>Unified Modeling Language: Superstructure, Version 2.2</em>. Object Management Group. Retrieved from <a href="http://www.omg.org/cgi-bin/doc?formal/2009-02-02" class="bare">http://www.omg.org/cgi-bin/doc?formal/2009-02-02</a></p>
			</div>
			</div>
			</div>''',
			'''
			«config»
			This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They'll be happy now!

			bibliography::[]

			''');
	}

	@Test
	public def void testMultipleCitationsWithBibliography() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some text! [<a href="#OMG09a">OMG09a(p.1, p.2)</a>] Yup, cited those guys. They&#8217;ll be happy now!</p>
			</div>
			<div class="paragraph">
			<p>Here&#8217;s another great citation [<a href="#Canning89a">Canning89a</a>].</p>
			</div>
			<div class="openblock bibliography">
			<div class="content">
			<div class="paragraph">
			<p><a id="OMG09a"></a>OMG. (2009). <em>Unified Modeling Language: Superstructure, Version 2.2</em>. Object Management Group. Retrieved from <a href="http://www.omg.org/cgi-bin/doc?formal/2009-02-02" class="bare">http://www.omg.org/cgi-bin/doc?formal/2009-02-02</a></p>
			</div>
			<div class="paragraph">
			<p><a id="Canning89a"></a>Canning, Peter and Cook, William and Hill, Walter and Olthoff, Walter and Mitchell, John C.. (1989). <em>F-bounded Polymorphism for Object-oriented Programming</em>. Retrieved from <a href="http://doi.acm.org/10.1145/99370.99392" class="bare">http://doi.acm.org/10.1145/99370.99392</a></p>
			</div>
			</div>
			</div>''',
			'''
			«config»
			This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They'll be happy now!

			Here's another great citation cite:[Canning89a].

			bibliography::[]

			''');
	}

	@Test
	public def void testOneCitationWithBibliographyAndAvoidDuplicateEntries() {
		// See https://github.numberfour.eu/NumberFour/asciispec/issues/45
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some text! [<a href="#OMG09a">OMG09a(p.1, p.2)</a>] Yup, cited those guys. They&#8217;ll be happy now! Holy crap, I&#8217;m citing them again! [<a href="#OMG09a">OMG09a</a>] Yup, done it! And there must only be ONE entry in the bibliography!</p>
			</div>
			<div class="openblock bibliography">
			<div class="content">
			<div class="paragraph">
			<p><a id="OMG09a"></a>OMG. (2009). <em>Unified Modeling Language: Superstructure, Version 2.2</em>. Object Management Group. Retrieved from <a href="http://www.omg.org/cgi-bin/doc?formal/2009-02-02" class="bare">http://www.omg.org/cgi-bin/doc?formal/2009-02-02</a></p>
			</div>
			</div>
			</div>''',
			'''
			«config»
			This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They'll be happy now! Holy crap, I'm citing them again! cite:[OMG09a] Yup, done it! And there must only be ONE entry in the bibliography!

			bibliography::[]

			''');
	}

	@Test
	public def void testMissingConfig() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They&#8217;ll be happy now!</p>
			</div>''',
			'''
			This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They'll be happy now!
			''',
			"asciispec  : ERROR: line 1: Could not find a BibTeX file");
	}

	@Test
	public def void testMissingConfigWhenCitationIsCalledMultipleTimes() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They&#8217;ll be happy now!</p>
			</div>
			<div class="paragraph">
			<p>cite:[OMG09a(1,2)]</p>
			</div>
			<div class="paragraph">
			<p>cite:[OMG09a(1,2)]</p>
			</div>''',
			'''
			This is some text! cite:[OMG09a(1,2)] Yup, cited those guys. They'll be happy now!

			cite:[OMG09a(1,2)]

			cite:[OMG09a(1,2)]
			''',
			"asciispec  : ERROR: line 1: Could not find a BibTeX file");
	}
}