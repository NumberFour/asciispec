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
 * Test cases for {@link InlineRequirementLinkProcessor}.
 */
class InlineRequirementLinkProcessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		new InlineRequirementLinkExtension().register(doc);
	}
	private def getConfig() ''':linkattrs:'''
	
	@Test
	public def void testValidLinkWithVersion() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <a href="#Req-SLR-1" title="SLR-1">SLR-1</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req:SLR-1:1[] and so on.
			 
			More lines for good measure.'''
		);
	}
	
	@Test
	public def void testValidLinkWithoutVersion() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <a href="#Req-SLR-1" title="SLR-1">SLR-1</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req:SLR-1[] and so on.
			 
			More lines for good measure.'''
		);
	}
	
	@Test
	public def void testInvalidLinkWithMissingVersion() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is some inline text <a href="#Req-SLR-1" title="SLR-1">SLR-1</a> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req:SLR-1:[] and so on.
			 
			More lines for good measure.'''
		);
	}
	
	@Test
	public def void testInvalidLinkWithMissingID() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Requirement reference is invalid: 'req::1[]']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req::1[] and so on.
			 
			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Requirement reference is invalid: 'req::1[]'"
		);
	}
	
	@Test
	public def void testInvalidLinkWithMissingIDAndVersion() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Requirement reference is invalid: 'req::[]']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req::[] and so on.
			 
			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Requirement reference is invalid: 'req::[]'"
		);
	}
	
	@Test
	public def void testInvalidLinkWithInvalidVersion() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Requirement reference is invalid: 'req:SRL-1:asdf[]']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req:SRL-1:asdf[] and so on.
			 
			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Requirement reference is invalid: 'req:SRL-1:asdf[]'"
		);
	}
	
	@Test
	public def void testInvalidLinkWithInvalidVersion2() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>This is some inline text <mark>[Error: Requirement reference is invalid: 'req:SRL-1:-1[]']</mark> and so on.</p>
			</div>
			<div class="paragraph">
			<p>More lines for good measure.</p>
			</div>''',
			'''
			«config»
			This is some inline text req:SRL-1:-1[] and so on.
			 
			More lines for good measure.''',
			"asciispec  : ERROR: line 2: Requirement reference is invalid: 'req:SRL-1:-1[]'"
		);
	}
}
