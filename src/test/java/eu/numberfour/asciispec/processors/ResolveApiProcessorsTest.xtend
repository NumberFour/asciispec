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
class ResolveApiProcessorsTest extends AsciidoctorTest {

	@Before
	def public void registerExtensions() {
		new ResolveFindExtension().register(doc);
		new ResolveApiExtension().register(doc);
		new InlineWikiLinkExtension().register(doc);
		new SourceLinkExtension().register(doc);
	}

	@Test
	def void simple1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element. No leveloffset.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">
			<div class="sect2 memberdoc">
			<h3 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format">Method format</h3>
			<div class="sect3">
			<h4 id="_signature">Signature</h4>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>

			</div>
			</div>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element. No leveloffset.

			include::{api}DateTimeFormat#format[]
			''');
	}

	@Test
	def void include1() throws IOException {
		convertFileAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include a subsection:</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_chapter_1_1"><a class="anchor" href="#_chapter_1_1"></a>1. Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			<div class="sect2 memberdoc">
			<h3 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.static.withStyle"><a class="anchor" href="#gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.static.withStyle"></a>1.1. Static method withStyle</h3>
			<div class="sect3">
			<h4 id="_signature"><a class="anchor" href="#_signature"></a>1.1.1. Signature</h4>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L18" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle" target="_blank"><code>project withStyle(style: any, emphasis: DateTimeFormatEmphasis=…): DateTimeFormat</code></a></p>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics"><a class="anchor" href="#_semantics"></a>1.1.2. Semantics</h4>
			<div class="openblock todo">
			<div class="content">
			<div class="paragraph">
			<p>Add tests specifying semantics for <code>withStyle(style: any, emphasis: DateTimeFormatEmphasis=…): DateTimeFormat</code></p>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L15" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat" target="_blank"></a></p>
			</div>
			</div>
			</div>
			</div>
			</div>''',
			"src/test/resources/data/api_resolver",
			"include1.adoc");
	}

	@Test
	def void leveloffset1() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with leveloffset +1.</p>
			</div>
			</div>
			</div>
			<div class="sect2">
			<h3 id="_class_datetimeformat">Class DateTimeFormat</h3>
			<div class="sect3 memberdoc">
			<h4 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format">Method format</h4>
			<div class="sect4">
			<h5 id="_signature">Signature</h5>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect4">
			<h5 id="_semantics">Semantics</h5>

			</div>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with leveloffset +1.

			include::{api}DateTimeFormat#format[leveloffset=+1]
			''');
	}

	@Test
	def void leveloffset2() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with leveloffset 0.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">
			<div class="sect2 memberdoc">
			<h3 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format">Method format</h3>
			<div class="sect3">
			<h4 id="_signature">Signature</h4>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics">Semantics</h4>

			</div>
			</div>
			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with leveloffset 0.

			include::{api}DateTimeFormat#format[leveloffset=0]
			''');
	}

	@Test
	def void leveloffset3() throws IOException {
		convertAndAssert(
		'''
			<h1 id="_class_datetimeformat" class="sect0">Class DateTimeFormat</h1>
			<div class="sect1 memberdoc">
			<h2 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format">Method format</h2>
			<div class="sectionbody">
			<div class="sect2">
			<h3 id="_signature">Signature</h3>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect2">
			<h3 id="_semantics">Semantics</h3>
			<div class="paragraph">
			<p>We included an API element with leveloffset -1.</p>
			</div>
			</div>
			</div>
			</div>''',
			'''
			«config»

			include::{api}DateTimeFormat#format[leveloffset=-1]

			We included an API element with leveloffset -1.
			''');
	}

	@Test
	def void lines1() throws IOException {
		convertAndAssert(
		'''
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines=1]
			''');
	}

	@Test
	def void lines2() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines=1;2;3]
			''');
	}

	@Test
	def void lines3() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="1,2,3"]
			''');
	}

	@Test
	def void lines4() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="1..3"]
			''');
	}

	@Test
	def void lines5() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="1..2,3"]
			''');
	}

	@Test
	def void lines6() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="1,2..3"]
			''');
	}

	@Test
	def void lines7() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="-1,0..2,3"]
			''');
	}

	@Test
	def void lines8() throws IOException {
		convertAndAssert(
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat">Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element with one line.

			include::{api}DateTimeFormat#format[lines="1..4,5"]
			''');
	}

	@Test
	def void err1() throws IOException {
		convertStringAndAssertErrorContains(
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}DateTimeFormat#formatXXX[]
			<mark>[Error: PQN not found]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element.

			include::{api}DateTimeFormat#formatXXX[]
			''',
			'''
			asciispec  : ERROR: line 10: PQN not found: 'DateTimeFormat#formatXXX'.''');
	}

	@Test
	def void err2() throws IOException {
		convertStringAndAssertErrorContains(
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}format[]
			<mark>[Error: Ambiguous PQN]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element.

			include::{api}format[]
			''',
			'''
			asciispec  : ERROR: line 10: PQN is ambiguous: 'format'.''');
	}

	@Test
	def void err3() throws IOException {
		convertStringAndAssertErrorContains(
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}"DateTimeFormat#format[]
			<mark>[Error: PQN malformed]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element.

			include::{api}"DateTimeFormat#format[]
			''',
			'''
			asciispec  : ERROR: line 10: PQN could not be parsed: 'include:{api}"DateTimeFormat#format[]'.''');
	}

	@Test
	def void err4() throws IOException {
		convertStringAndAssertErrorContains(
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}IterableExt#reduce[]
			<mark>[Error: Could not read module file: src/test/resources/data/docu/gen_adoc/modules/stdlib_api#packages/eu.numberfour.n4js.base.api#src.n4js/n4.lang/IterableExt.adoc]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element.

			include::{api}IterableExt#reduce[]
			''',
			'''
			asciispec  : ERROR: line 10: Could not read module file: src/test/resources/data/docu/gen_adoc/modules/stdlib_api#packages/eu.numberfour.n4js.base.api#src.n4js/n4.lang/IterableExt.adoc''');
	}

	@Test
	def void err5() throws IOException {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}IterableExt#reduce[lines=""1..3"]
			<mark>[Error: Could not parse given attributes: lines=""1..3"]</mark></p>
			</div>''',
			'''
			«config»

			= Main Document Title

			We include an API element.

			include::{api}IterableExt#reduce[lines="1..3]
			''',
			'''
			asciispec  : ERROR: line 10: Could not parse given attributes: lines=""1..3"''');
	}

	private def getConfig() '''
			:linkattrs:
			:gen_adoc_dir: src/test/resources/data/docu/gen_adoc
			:srclnk_repo_def: stdlib_api;Standard lib API;https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/{CMS_PATH}#L{LINE_NO}

		'''
}
