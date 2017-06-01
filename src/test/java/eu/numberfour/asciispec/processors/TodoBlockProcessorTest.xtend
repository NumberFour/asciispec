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
class TodoBlockProcessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		registerRubyExtensionBlock("TODO","/ext/todo/extension.rb","TodoBlock");
	}

	@Test
	public def void testTodo() {
		convertAndAssert(
		'''
		<div class="admonitionblock todo">
		<table>
		<tr>
		<td class="icon">
		<div class="title">Todo</div>
		</td>
		<td class="content">
		<div class="paragraph">
		<p>my reminder</p>
		</div>
		</td>
		</tr>
		</table>
		</div>''',
		'''
		[TODO]
		====
		my reminder
		====
		''');
	}
	
}