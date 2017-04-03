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
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element. No leveloffset.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">
			<div class="sect2 memberdoc">
			<h3 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"><a class="anchor" href="#gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"></a>1.1. Method format</h3>
			<div class="sect3">
			<h4 id="_signature"><a class="anchor" href="#_signature"></a>1.1.1. Signature</h4>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics"><a class="anchor" href="#_semantics"></a>1.1.2. Semantics</h4>

			</div>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"simple1.adoc");
	}

	@Test
	def void include1() throws IOException {
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
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"include1.adoc");
	}

	@Test
	def void leveloffset1() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with leveloffset +1.</p>
			</div>
			</div>
			</div>
			<div class="sect2">
			<h3 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h3>
			<div class="sect3 memberdoc">
			<h4 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"><a class="anchor" href="#gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"></a>1.1. Method format</h4>
			<div class="sect4">
			<h5 id="_signature"><a class="anchor" href="#_signature"></a>Signature</h5>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect4">
			<h5 id="_semantics"><a class="anchor" href="#_semantics"></a>Semantics</h5>

			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"leveloffset1.adoc");
	}

	@Test
	def void leveloffset2() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with leveloffset 0.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">
			<div class="sect2 memberdoc">
			<h3 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"><a class="anchor" href="#gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"></a>1.1. Method format</h3>
			<div class="sect3">
			<h4 id="_signature"><a class="anchor" href="#_signature"></a>1.1.1. Signature</h4>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect3">
			<h4 id="_semantics"><a class="anchor" href="#_semantics"></a>1.1.2. Semantics</h4>

			</div>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"leveloffset2.adoc");
	}

	@Test
	def void leveloffset3() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with leveloffset -1.</p>
			</div>
			</div>
			</div>
			<h1 id="_class_datetimeformat" class="sect0"><a class="anchor" href="#_class_datetimeformat"></a>Class DateTimeFormat</h1>
			<div class="sect1 memberdoc">
			<h2 id="gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"><a class="anchor" href="#gsec:spec_n4.format.DateTimeFormat.DateTimeFormat.format"></a>1. Method format</h2>
			<div class="sectionbody">
			<div class="sect2">
			<h3 id="_signature"><a class="anchor" href="#_signature"></a>1.1. Signature</h3>
			<div class="paragraph">
			<p><a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DateTimeFormat.n4jsd#L20" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat#format" target="_blank"><code>public format(value: DateTime): StructuredText</code></a></p>
			</div>
			</div>
			<div class="sect2">
			<h3 id="_semantics"><a class="anchor" href="#_semantics"></a>1.2. Semantics</h3>

			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"leveloffset3.adoc");
	}

	@Test
	def void lines1() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines1.adoc");
	}

	@Test
	def void lines2() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines2.adoc");
	}

	@Test
	def void lines3() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines3.adoc");
	}

	@Test
	def void lines4() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines4.adoc");
	}

	@Test
	def void lines5() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines5.adoc");
	}

	@Test
	def void lines6() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines6.adoc");
	}

	@Test
	def void lines7() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines7.adoc");
	}

	@Test
	def void lines8() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>We include an API element with one line.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_class_datetimeformat"><a class="anchor" href="#_class_datetimeformat"></a>1. Class DateTimeFormat</h2>
			<div class="sectionbody">

			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/api_resolver",
			"lines8.adoc");
	}

	@Test
	def void err1() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}DateTimeFormat#formatXXX[]
			<mark>[Error: PQN not found]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"err1.adoc",
			'''
			asciispec  : ERROR: err1.adoc: line 7: PQN not found: 'DateTimeFormat#formatXXX'.''');
	}

	@Test
	def void err2() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}format[]
			<mark>[Error: Ambiguous PQN]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"err2.adoc",
			'''
			asciispec  : ERROR: err2.adoc: line 7: PQN is ambiguous: 'format'.''');
	}

	@Test
	def void err3() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}"DateTimeFormat#format[]
			<mark>[Error: PQN malformed]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"err3.adoc",
			'''
			asciispec  : ERROR: err3.adoc: line 7: PQN could not be parsed: 'include:{api}"DateTimeFormat#format[]'.''');
	}

	@Test
	def void err4() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}IterableExt#reduce[]
			<mark>[Error: Could not read module file: /Users/marcus.mews/GitHub/asciispec/src/test/resources/data/docu/gen_adoc/modules/stdlib_api#packages/eu.numberfour.n4js.base.api#src.n4js/n4.lang/IterableExt.adoc]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"err4.adoc",
			'''
			asciispec  : ERROR: err4.adoc: line 7: Could not read module file: /Users/marcus.mews/GitHub/asciispec/src/test/resources/data/docu/gen_adoc/modules/stdlib_api#packages/eu.numberfour.n4js.base.api#src.n4js/n4.lang/IterableExt.adoc''');
	}

	@Test
	def void err5() throws IOException {
		'''
			<div class="paragraph">
			<p>We include an API element.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}IterableExt#reduce[lines=""1..3"]
			<mark>[Error: Could not parse given attributes: lines=""1..3"]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"err5.adoc",
			'''
			asciispec  : ERROR: err5.adoc: line 7: Could not parse given attributes: lines=""1..3"''');
	}
}
