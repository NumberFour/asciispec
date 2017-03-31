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
	def void test() throws IOException {
		'''
			<div class="paragraph">
			<p>First we include an API element. No leveloffset.</p>
			</div>
			<div class="paragraph">
			<p>include:{api}DateTimeFormat[]
			<mark>[Error: Could not read module file: ../../docu/gen_adoc/modules/stdlib_api#packages/eu.numberfour.stdlib.format.api#src.n4js/n4.format/DateTimeFormat.adoc]</mark></p>
			</div>'''
		.convertFileAndAssertErrorContains(
			"src/test/resources/data/api_resolver",
			"top.adoc",
			'''
			asciispec  : WARN: noCyclicExceptionsHelper1.adoc: line 8: Cannot detect circular dependencies. Probably 'include' was used without '{find}' macro when including file:''');
	}
}
