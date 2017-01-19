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
 * Test cases for {@link RequirementBlockProcessor}.
 */
class RequirementBlockProcessorTest extends AsciidoctorTest {
	@Before
	public def void registerExtensions() {
		new RequirementBlockExtension().register(doc);
	}

	@Test
	public def void testEmptyBodyWithTitle() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">An Important Requirement</a> (ver. 1)</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=RSL-2,version=1]
			--
			--
			'''
		);
	}

	@Test
	public def void testWithTitleAndBody() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">An Important Requirement</a> (ver. 1)</p>
			</div>
			<div class="paragraph">
			<p>This requirement describes an important thing. Here&#8217;s what&#8217;s up:</p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>My heart rate when this test fails.</p>
			</li>
			<li>
			<p>Build times in general.</p>
			</li>
			<li>
			<p>And nothing else.</p>
			</li>
			</ul>
			</div>
			<div class="paragraph">
			<p>Also I&#8217;m going to have to go ahead and ask you to come in on Sunday as well. Yeah.</p>
			</div>
			<div class="paragraph">
			<p>So, if you could do that, that&#8217;d be really great.</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=RSL-2,version=1]
			--
			This requirement describes an important thing. Here's what's up:

			- My heart rate when this test fails.
			- Build times in general.
			- And nothing else.
			
			Also I'm going to have to go ahead and ask you to come in on Sunday as well. Yeah.
			
			So, if you could do that, that'd be really great.
			--
			'''
		);
	}

	@Test
	public def void testMissingID() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="missing_id"></a><strong>Req. MISSING ID:</strong> <a href="#missing_id">An Important Requirement</a> (ver. 1)</p>
			</div>
			<div class="openblock issues">
			<div class="content">
			<div class="paragraph">
			<p><strong>Issues:</strong></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Requirement ID is missing</p>
			</li>
			</ul>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>This requirement describes an important thing.</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,version=1]
			--
			This requirement describes an important thing.
			--
			'''
		);
	}
	
	@Test
	public def void testNumericalID() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-1234"></a><strong>Req. 1234:</strong> <a href="#Req-1234">An Important Requirement</a> (ver. 1)</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=1234,version=1]
			--
			--
			'''
		);
	}
	
	@Test
	public def void testMissingVersion() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">An Important Requirement</a> (INVALID VERSION)</p>
			</div>
			<div class="openblock issues">
			<div class="content">
			<div class="paragraph">
			<p><strong>Issues:</strong></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Requirement version is missing</p>
			</li>
			</ul>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>This requirement describes an important thing.</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=RSL-2]
			--
			This requirement describes an important thing.
			--
			'''
		);
	}
	
	@Test
	public def void testNegativeVersion() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">An Important Requirement</a> (INVALID VERSION)</p>
			</div>
			<div class="openblock issues">
			<div class="content">
			<div class="paragraph">
			<p><strong>Issues:</strong></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Requirement version must be a nonnegative integer, but is -1</p>
			</li>
			</ul>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>This requirement describes an important thing.</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=RSL-2,version=-1]
			--
			This requirement describes an important thing.
			--
			'''
		);
	}
	
	@Test
	public def void testUnparseableVersion() {
		convertAndAssert(
			'''
			<div class="openblock requirement">
			<div class="content">
			<div class="paragraph">
			<p><a id="Req-RSL-2"></a><strong>Req. RSL-2:</strong> <a href="#Req-RSL-2">An Important Requirement</a> (INVALID VERSION)</p>
			</div>
			<div class="openblock issues">
			<div class="content">
			<div class="paragraph">
			<p><strong>Issues:</strong></p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Requirement version is invalid: 'd'</p>
			</li>
			</ul>
			</div>
			</div>
			</div>
			<div class="paragraph">
			<p>This requirement describes an important thing.</p>
			</div>
			</div>
			</div>''',
			'''
			.An Important Requirement
			[req,id=RSL-2,version=d]
			--
			This requirement describes an important thing.
			--
			'''
		);
	}
}
