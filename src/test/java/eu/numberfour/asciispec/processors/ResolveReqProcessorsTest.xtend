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
import java.io.IOException
import org.junit.Before
import org.junit.Test

/**
 *
 */
class ResolveReqProcessorsTest extends AsciidoctorTest {

	@Before
	def public void registerExtensions() {
		new ResolveReqExtension().register(doc);
		new SourceLinkExtension().register(doc);
	}

	@Test
	def void respectCommentRegions1() throws IOException {
		convertAndAssert(
		'''
			<div class="paragraph">
			<p>nothing else</p>
			</div>''',
			'''
			= Main Document Title

			////
			We include an Req element.
			include::{req}1011[]
			////
			nothing else
			''');
	}

	@Test
	def void respectCommentRegions2() throws IOException {
		convertAndAssert(
		'''
			<div class="paragraph">
			<p>nothing else</p>
			</div>''',
			'''
			= Main Document Title

			//We include an Req element.
			//include::{req}1011[]
			nothing else
			''');
	}

	@Test
	def void simple1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			<dt class="hdlist1">1 receiver is undefined</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_undefined[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element.

			include::{req}1011[]
			''');
	}
	@Test
	def void leveloffset1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with leveloffset +1.</p>
			</div>
			</div>
			</div>
			<div class="sect4">
			<h5 id="_semantics">Semantics</h5>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			<dt class="hdlist1">1 receiver is undefined</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_undefined[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with leveloffset +1.

			include::{req}1011[leveloffset=+1]
			''');
	}

	@Test
	def void leveloffset2() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with leveloffset 0.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			<dt class="hdlist1">1 receiver is undefined</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_undefined[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with leveloffset 0.

			include::{req}1011[leveloffset=0]
			''');
	}

	@Test
	def void leveloffset3() throws IOException {
		convertAndAssert(
		'''
			<div class="sect2">
			<h3 id="_semantics">Semantics</h3>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			<dt class="hdlist1">1 receiver is undefined</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_undefined[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			<div class="paragraph">
			<p>We included an Req element with leveloffset -1.</p>
			</div>
			</div>''',
			'''
			«config»

			include::{req}1011[leveloffset=-1]

			We included an Req element with leveloffset -1.
			''');
	}

	@Test
	def void lines1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>

			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines=1]
			''');
	}

	@Test
	def void lines2() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines=1;2;3]
			''');
	}

	@Test
	def void lines3() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="1,2,3"]
			''');
	}

	@Test
	def void lines4() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="1..3"]
			''');
	}

	@Test
	def void lines5() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="1..2,3"]
			''');
	}

	@Test
	def void lines6() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="1,2..3"]
			''');
	}

	@Test
	def void lines7() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="-1,0..2,3"]
			''');
	}

	@Test
	def void lines8() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an Req element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark></p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element with one line.

			include::{req}1011[lines="1..4,5"]
			''');
	}

	@Test
	def void linesAndLeveloffset1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect4">
			<h5 id="_semantics">Semantics</h5>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark>
			:leveloffset: -1</p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{req}1011[leveloffset=+1, lines="1..3"]
				''');
	}

	@Test
	def void linesAndLeveloffset2() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect4">
			<h5 id="_semantics">Semantics</h5>
			<div class="dlist">
			<dl>
			<dt class="hdlist1">1 receiver is null</dt>
			<dd>
			<p>srclnk:stdlib_api:packages:eu.numberfour.stdlib.model.base.api-tests:test/n4js/n4/util/PSBaseSelectionExecTest:PathSelectorBaseSelectionExecTest#_1_receiver_is_null[Test]
			<mark>[Error: PQN not found]</mark>
			:leveloffset: -1</p>
			</dd>
			</dl>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{req}1011[lines="1..3", leveloffset=+1]
				''');
	}

	@Test
	def void err1() throws IOException {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>We include an Req element.</p>
			</div>
			<div class="paragraph">
			<p>include:{req}XX-1011[]
			<mark>[Error: Could not read requirement file: src/test/resources/data/docu/gen_adoc/requirements/XX-1011.adoc]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element.

			include::{req}XX-1011[]
			''',
			'''
			asciispec  : ERROR: line 10: Could not read requirement file: src/test/resources/data/docu/gen_adoc/requirements/XX-1011.adoc''');
	}

	@Test
	def void err2() throws IOException {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>We include an Req element.</p>
			</div>
			<div class="paragraph">
			<p>include:{req}1011[lines=""1"]
			<mark>[Error: Could not parse attributes: lines=""1"]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an Req element.

			include::{req}1011[lines="1]
			''',
			'''
			asciispec  : ERROR: line 10: Could not parse attributes: lines=""1"''');
	}

	private def getConfig() '''
			:linkattrs:
			:gen_adoc_dir: src/test/resources/data/docu/gen_adoc
			:srclnk_repo_def: stdlib_api;Standard lib API;https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/{CMS_PATH}#L{LINE_NO}

		'''
}
