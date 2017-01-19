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
 * Test cases for {@link DefinitionBlockProcessor}.
 */
class DefinitionBlockProcessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new DefinitionBlockExtension().register(doc);
	}

	@Test
	public def void testEmptyBodyWithTitle() {
		convertAndAssert(
			'''
			<div class="openblock definition">
			<div class="content">
			<div class="paragraph">
			<p><a id="my_design"></a><strong>Definition:</strong> <a href="#my_design">My Design</a></p>
			</div>
			</div>
			</div>''',
			'''
			.My Design
			[def]
			--
			--
			'''
		);
	}

	@Test
	public def void testEmptyBodyWithMissingTitle() {
		convertAndAssert(
			'''
			<div class="openblock definition">
			<div class="content">
			<div class="paragraph">
			<p><a id="missing_definition_title"></a><strong>Definition:</strong> <a href="#missing_definition_title">MISSING DEFINITION TITLE</a></p>
			</div>
			</div>
			</div>''',
			'''
			[def]
			--
			--
			'''
		);
	}
	
	@Test
	public def void testBodyAndTitle() {
		convertAndAssert(
			'''
			<div class="openblock definition">
			<div class="content">
			<div class="paragraph">
			<p><a id="this_is_my_design"></a><strong>Definition:</strong> <a href="#this_is_my_design">This is my design</a></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>something</p>
			</li>
			<li>
			<p>something else</p>
			</li>
			</ul>
			</div>
			<div class="paragraph">
			<p>and some final text</p>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>But the definition should be over now!</p>
			</div>''',
			'''
			.This is my design
			[def]
			--
			* something
			* something else
			 
			and some final text
			--
			But the definition should be over now!
			'''
		)
	}
}