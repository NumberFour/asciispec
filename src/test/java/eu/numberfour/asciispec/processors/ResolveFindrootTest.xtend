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
import java.util.HashMap

/**
 *
 */
class ResolveFindrootTest extends AsciidoctorTest {


	@Before
	def public void registerExtensions() {
		new ResolveFindExtension().register(doc);
		new SourceLinkExtension().register(doc);
	}

	@Test
	def void testChap1_1FileOk1() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "data");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_1.adoc",
			arguments);
	}

	@Test
	def void testChap1_1FileOk2() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "find_resolver");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_1.adoc",
			arguments);
	}

	@Test
	def void testChap1_1FileOk3() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "data/find_resolver");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_1.adoc",
			arguments);
	}

	@Test
	def void testChap1_1FileOk4() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "find_resolver");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_4</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssert(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_4.adoc",
			arguments);
	}

	@Test
	def void testChap1_1FileErr1() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "sub1");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>include::{find}configs/config.adoc[1="ONCE"] <mark>[Error: File 'configs/config.adoc' could not be found]</mark> </p>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_1.adoc",
			arguments,
			'''
			asciispec  : ERROR: chap1_1.adoc: line 3: File 'configs/config.adoc' could not be found
			''');
	}

	@Test
	def void testChap1_1FileErr2() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "find_resolver/sub1");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>include::{find}configs/config.adoc[1="ONCE"] <mark>[Error: File 'configs/config.adoc' could not be found]</mark> </p>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_1.adoc",
			arguments,
			'''
			asciispec  : ERROR: chap1_1.adoc: line 3: File 'configs/config.adoc' could not be found
			''');
	}

	@Test
	def void testChap1_1FileErr3() throws IOException {
		val arguments = new HashMap<String, Object>();

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p><mark>[Error: Variable 'findroot' is only allowed as a command line argument.]</mark> sub1</p>
			</div>
			<div class="paragraph">
			<p>This is from chapter file chap1_1</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_3.adoc",
			arguments,
			'''
			asciispec  : ERROR: chap1_3.adoc: line 3: Variable 'findroot' is only allowed as a command line argument.
			''');
	}

	@Test
	def void testChap1_1FileErr4() throws IOException {
		val arguments = new HashMap<String, Object>();
		arguments.put("findroot", "sub1");

		'''
			<div class="sect1">
			<h2 id="_chapter_1_1">Chapter 1_1</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>This is from chapter file chap1_4</p>
			</div>
			</div>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/find_resolver/sub1",
			"chap1_4.adoc",
			arguments,
			'''
			asciispec  : ERROR: chap1_4.adoc: line 4: File 'find_resolver/sub1/chap1_4.adoc' could not be found
			''');
	}

}
