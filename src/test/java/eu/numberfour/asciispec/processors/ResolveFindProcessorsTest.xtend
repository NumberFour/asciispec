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
class ResolveFindProcessorsTest extends AsciidoctorTest {

	@Before
	def public void registerExtensions() {
		new ResolveFindExtension().register(doc);
		new InlineWikiLinkExtension().register(doc);
		new SourceLinkExtension().register(doc);
	}

	@Test
	def void testTopFile() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>First, we show an image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="images/no4.png" alt="no4">
			</div>
			</div>
			<div class="paragraph">
			<p>Then we try the cwiki macro: <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/www.google.de"><img src="icons/jira.png" alt="Google" title="Google"></a></span><a href="https://confluence.numberfour.eu/display/www.google.de" title="Google">Confluence entry:Google</a></p>
			</div>
			<div class="paragraph">
			<p>Here comes chapter 1_2</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_chapter_1_1"><a class="anchor" href="#_chapter_1_1"></a>1. Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			<div class="paragraph">
			<p>Here comes chapter 1_1</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_chapter_1_2"><a class="anchor" href="#_chapter_1_2"></a>2. Chapter 1_2</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_2</p>
			</div>
			<div class="paragraph">
			<p>Here comes chapter 1_2_1</p>
			</div>
			<div class="sect2">
			<h3 id="_chapter_1_2_1"><a class="anchor" href="#_chapter_1_2_1"></a>2.1. Chapter 1_2_1</h3>
			<div class="paragraph">
			<p>First, we show an image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="images/no4.png" alt="no4">
			</div>
			</div>
			<div class="paragraph">
			<p>Now, we show a second image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="sub1/sub1sub1/gate.png" alt="gate">
			</div>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_2_1</p>
			</div>
			<div class="paragraph">
			<p>Then we try again the cwiki macro: <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/www.google.de"><img src="icons/jira.png" alt="Google2" title="Google2"></a></span><a href="https://confluence.numberfour.eu/display/www.google.de" title="Google2">Confluence entry:Google2</a></p>
			</div>
			<div class="paragraph">
			<p>Also, we try the srclnk macro: <a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DurationFormats.n4jsd#L92" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DurationFormats:TimeSpanPatternFormat@getStartWithQuantityFormat" target="_blank">getStartWithQuantityFormat()</a></p>
			</div>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver", "top.adoc");
	}

	@Test
	def void testChap1_1File() throws IOException {
		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver/sub1", "chap1_1.adoc");
	}

	@Test
	def void testChap1_2File() throws IOException {
		'''
			<div class="sect1">
			<h2 id="_chapter_1_2">Chapter 1_2</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_2</p>
			</div>
			<div class="paragraph">
			<p>Here comes chapter 1_2_1</p>
			</div>
			<div class="sect2">
			<h3 id="_chapter_1_2_1"><a class="anchor" href="#_chapter_1_2_1"></a>1.1. Chapter 1_2_1</h3>
			<div class="paragraph">
			<p>First, we show an image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="../images/no4.png" alt="no4">
			</div>
			</div>
			<div class="paragraph">
			<p>Now, we show a second image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="sub1sub1/gate.png" alt="gate">
			</div>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_2_1</p>
			</div>
			<div class="paragraph">
			<p>Then we try again the cwiki macro: <span class="image"><a class="image" href="https://confluence.numberfour.eu/display/www.google.de"><img src="icons/jira.png" alt="Google2" title="Google2"></a></span><a href="https://confluence.numberfour.eu/display/www.google.de" title="Google2">Confluence entry:Google2</a></p>
			</div>
			<div class="paragraph">
			<p>Also, we try the srclnk macro: <a href="https://github.numberfour.eu/NumberFour/stdlib_api/blob/master/packages/eu.numberfour.stdlib.format.api/src/n4js/n4/format/DurationFormats.n4jsd#L92" title="stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DurationFormats:TimeSpanPatternFormat@getStartWithQuantityFormat" target="_blank">getStartWithQuantityFormat()</a></p>
			</div>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver/sub1", "chap1_2.adoc");
	}

	@Test
	def void testChap1_2_1File() throws IOException {
		'''
			<div class="sect2">
			<h3 id="_chapter_1_2_1">Chapter 1_2_1</h3>
			<div class="paragraph">
			<p>First, we show an image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="../../images/no4.png" alt="no4">
			</div>
			</div>
			<div class="paragraph">
			<p>Now, we show a second image:</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="gate.png" alt="gate">
			</div>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_2_1</p>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver/sub1/sub1sub1", "chap1_2_1.adoc");
	}

	@Test
	def void testNoFindInclude() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document"><a class="anchor" href="#_copy_of_main_document"></a>1. Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Top Top Top</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"noFindInclude.adoc",
			'''
			asciispec  : WARN: top.adoc: line 3: Cannot detect circular dependencies. Probably 'include' was used without '{find}' macro when including file:''');
	}

	@Test
	def void testMissingAdocExtension() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document"><a class="anchor" href="#_copy_of_main_document"></a>1. Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub2",
			"missingAdocExtension.adoc");
	}

	@Test
	def void testFileOnce1() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>Once1 Once1 Once1</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper">Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver/sub2", "includeFileOnce1.adoc");
	}

	@Test
	def void testFileOnceException1() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper">Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document_2"><a class="anchor" href="#_copy_of_main_document_2"></a>1. Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"includeFileOnce2.adoc",
			'''
			asciispec  : WARN: includeFileOnce2.adoc: line 9: Inconsistent use of modifier FILE_ONCE at file 'toppot.adoc''');
	}

	@Test
	def void testFileOnceException2() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper"><a class="anchor" href="#_title_helper"></a>1. Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"includeFileOnce3.adoc",
			'''
			asciispec  : WARN: includeFileOnceHelper.adoc: line 8: Inconsistent use of modifier FILE_ONCE at file 'toppot.adoc''');
	}


	@Test
	def void testTargetOnce1() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>Once1 Once1 Once1</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper">Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver/sub2", "includeTargetOnce1.adoc");
	}

	@Test
	def void testTargetOnceException1() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper">Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document_2"><a class="anchor" href="#_copy_of_main_document_2"></a>1. Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"includeTargetOnce2.adoc",
			'''
			asciispec  : WARN: includeTargetOnce2.adoc: line 9: Inconsistent use of modifier TARGET_ONCE at file 'toppot.adoc''');
	}

	@Test
	def void testTargetOnceException2() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document">Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Toppot Toppot Toppot</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper"><a class="anchor" href="#_title_helper"></a>1. Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper Helper Helper</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"includeTargetOnce3.adoc",
			'''
			asciispec  : WARN: includeTargetOnceHelper.adoc: line 8: Inconsistent use of modifier TARGET_ONCE at file 'toppot.adoc''');
	}


	@Test
	def void testCircularDependencyExceptionSelf() throws IOException {
		'''
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			<div class="paragraph">
			<p>include::{find}circularDependencySelf.adoc[] <mark>[Error: Circular dependencies detected. More information in console output.]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"circularDependencySelf.adoc",
			'''
			asciispec  : ERROR: circularDependencySelf.adoc: line 9: A dependency cycle was detected. The file stack is:''');
	}


	@Test
	def void testCircularDependencyExceptionPair() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper"><a class="anchor" href="#_title_helper"></a>1. Title Helper</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			<div class="paragraph">
			<p>include::{find}circularDependencyPair.adoc[] <mark>[Error: Circular dependencies detected. More information in console output.]</mark></p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"circularDependencyPair.adoc",
			'''
			asciispec  : ERROR: circularDependencyPairHelper.adoc: line 9: A dependency cycle was detected. The file stack is:
				circularDependencyPair.adoc
				circularDependencyPairHelper.adoc''');
	}

	@Test
	def void testMultipleMatchesExceptionAtImage() throws IOException {
		'''
			<div class="paragraph">
			<p>This is from chapter file chap2_1</p>
			</div>
			<div class="imageblock">
			<div class="content">
			<img src="images/no4.png" alt="no4">
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"multipleLocationsImage.adoc",
			'''
			asciispec  : WARN: multipleLocationsImage.adoc: line 10: File 'images/no4.png' was found at multiple locations:
				- images/no4.png
				- ../images/no4.png''');
	}

	@Test
	def void testMultipleMatchesExceptionAtInclude() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_copy_of_main_document"><a class="anchor" href="#_copy_of_main_document"></a>1. Copy of Main Document</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Top Top Top</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"multipleLocationsInclude.adoc",
			'''
			asciispec  : WARN: multipleLocationsInclude.adoc: line 9: File 'top.adoc' was found at multiple locations:
				- top.adoc
				- ../top.adoc''');
	}


	@Test
	def void testFileNotFoundExceptionImage() throws IOException {
		'''
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			<div class="paragraph">
			<p>image::{find}no4.png <mark>[Error: File 'no4.png' could not be found]</mark> []</p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"fileNotFoundImage.adoc",
			'''
			asciispec  : ERROR: fileNotFoundImage.adoc: line 9: File 'no4.png' could not be found''');
	}

	@Test
	def void testFileNotFoundExceptionInclude() throws IOException {
		'''
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			<div class="paragraph">
			<p>include::{find}top2.adoc[] <mark>[Error: File 'top2.adoc' could not be found]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"fileNotFoundInclude.adoc",
			'''
			asciispec  : ERROR: fileNotFoundInclude.adoc: line 9: File 'top2.adoc' could not be found''');
	}

	@Test
	def void testFileNotFoundExceptionIncludeNoFind() throws IOException {
		'''
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			<div class="paragraph">
			<p>Unresolved directive in fileNotFoundIncludeNoFind.adoc - include::top2.adoc[]</p>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub2",
			"fileNotFoundIncludeNoFind.adoc");
	}

		@Test
	def void testTargetOnceMainFile() throws IOException {
		'''
			<div class="paragraph">
			<p>This is file "rootFolder/config.adoc".</p>
			</div>
			<div class="paragraph">
			<p>This is file "rootFolder/config2.adoc".</p>
			</div>
			<div class="paragraph">
			<p>Some text.</p>
			</div>
			<div class="paragraph">
			<p>This is file "outerFolder/fileA.adoc".</p>
			</div>
			<div class="paragraph">
			<p>Some more text.</p>
			</div>
			<div class="paragraph">
			<p>BEGINNING of file "rootFolder/subFolder/other.adoc".</p>
			</div>
			<div class="paragraph">
			<p>If processed directly, then "rootFolder/config.adoc" should show up here,
			if processed as included file, then nothing must show up here (avoid duplicate) (same line):</p>
			</div>
			<div class="paragraph">
			<p>If processed directly, then "rootFolder/subFolder/config2.adoc" should show up here,
			if processed as included file, then nothing must show up here (avoid duplicate) (same line):</p>
			</div>
			<div class="paragraph">
			<p>File from sibling sub-folder "subFolderB" should show up here (next line):</p>
			</div>
			<div class="paragraph">
			<p>This is file "rootFolder/subFolderB/fileB.adoc".</p>
			</div>
			<div class="paragraph">
			<p>END of file "rootFolder/subFolder/other.adoc".</p>
			</div>
			<div class="paragraph">
			<p>Even more text.</p>
			</div>'''
		.convertFileAndAssert("src/test/resources/data/find_resolver_target_once_modifier/rootFolder", "main.adoc");
	}

	@Test
	def void testTargetOnceOtherFile() throws IOException {
		'''
			<div class="paragraph">
			<p>BEGINNING of file "rootFolder/subFolder/other.adoc".</p>
			</div>
			<div class="paragraph">
			<p>If processed directly, then "rootFolder/config.adoc" should show up here,
			if processed as included file, then nothing must show up here (avoid duplicate) (same line):
			This is file "rootFolder/config.adoc".</p>
			</div>
			<div class="paragraph">
			<p>If processed directly, then "rootFolder/subFolder/config2.adoc" should show up here,
			if processed as included file, then nothing must show up here (avoid duplicate) (same line):
			This is file "rootFolder/subFolder/config2.adoc".</p>
			</div>
			<div class="paragraph">
			<p>File from sibling sub-folder "subFolderB" should show up here (next line):</p>
			</div>
			<div class="paragraph">
			<p>This is file "rootFolder/subFolderB/fileB.adoc".</p>
			</div>
			<div class="paragraph">
			<p>END of file "rootFolder/subFolder/other.adoc".</p>
			</div>'''
		.convertFileAndAssertErrorContains("src/test/resources/data/find_resolver_target_once_modifier/rootFolder/subFolder", "other.adoc", '''
			asciispec  : WARN: other.adoc: line 10: File 'config2.adoc' was found at multiple locations:
				- config2.adoc
				- ../config2.adoc''');
	}

	@Test
	def void testNoCyclicExceptions() throws IOException {
		'''
			<div class="sect1">
			<h2 id="_title">Title</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Main Main Main</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper_1">Title Helper 1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper1 Helper1 Helper1</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper_2">Title Helper 2</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper2 Helper2 Helper2</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_title_helper_3">Title Helper 3</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Helper3 Helper3 Helper3</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"noCyclicExceptions.adoc",
			'''
			asciispec  : WARN: noCyclicExceptionsHelper1.adoc: line 8: Cannot detect circular dependencies. Probably 'include' was used without '{find}' macro when including file:''');
	}

	@Test
	def void testIncludeSrclnkWithError() throws IOException {
		'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>These are letters.</p>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_a_small_subsection"><a class="anchor" href="#_a_small_subsection"></a>1. A Small Subsection</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>srclnk:getStartWithQuantityFormatX123[DREI getStartWithQuantityFormatX123()]
			<mark>[Error: PQN not found]</mark></p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub2",
			"includeSrclnkWithError.adoc",
			'''
			asciispec  : ERROR: includeSrclnkWithError.adoc: line 9: PQN not found: 'getStartWithQuantityFormatX123'.''');
	}
}
