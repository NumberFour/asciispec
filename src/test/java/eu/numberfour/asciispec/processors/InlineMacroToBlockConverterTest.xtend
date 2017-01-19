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
import org.junit.Ignore
import org.junit.Test

/**
 * Tests for {@link InlineMacroToBlockConverter}
 */
class InlineMacroToBlockConverterTest extends AsciidoctorTest {

	static class Extension extends ProcessorExtension {
		override register(JavaExtensionRegistry registry) {
			registry.inlineMacroToSiblingBlock("sidebar", new NopInlineMacroProcessor("nop"));
		}
	}

	@Before
	public def void registerExtensions() {
		new Extension().register(doc);
	}

	@Test
	public def testEmptyDocument() {
		convertAndAssert(
			'''''',
			''''''
		);
	}
	
	@Test
	public def testOneline() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>this is one line</p>
			</div>''',
			'''this is one line'''
		)
	}
	
	@Test
	public def testOneMacroInvocation() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>this is one  line</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			nop:some_target[]
			</div>
			</div>''',
			'''this is one nop:some_target[] line'''
		)
	}
	
	@Test
	public def testRecurseIntoSections() {
		convertAndAssert(
			'''
			<div id="preamble">
			<div class="sectionbody">
			<div class="paragraph">
			<p>Here&#8217;s a macro: .</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			nop:in_preamble[]
			</div>
			</div>
			</div>
			</div>
			<div class="sect1">
			<h2 id="_section">Section</h2>
			<div class="sectionbody">
			<div class="paragraph">
			<p>Here&#8217;s another one: </p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			nop:in_section[]
			</div>
			</div>
			</div>
			</div>''',
			'''
			= This is a document
			
			Here's a macro: nop:in_preamble[].
			
			== Section
			
			Here's another one: nop:in_section[]
			'''
		)
	}
	
	@Test
	public def testRecurseIntoTable() {
		convertAndAssert(
			'''
			<table class="tableblock frame-all grid-all spread">
			<colgroup>
			<col style="width: 14.2857%;">
			<col style="width: 57.1428%;">
			<col style="width: 28.5715%;">
			</colgroup>
			<tbody>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Type</p></th>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Source</p></th>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Output</p></th>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Italic</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">_italic phrase_</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><em>italic phrase</em></p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Bold Phrase</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">*bold phrase*</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>bold phrase</strong></p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Bold Letters</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">**b**old le**tt**ers</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>b</strong>old le<strong>tt</strong>ers</p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Bold Italic Phrase</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">*_bold italic phrase_*</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><strong><em>bold italic phrase</em></strong></p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Monospace</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">`monospace phrase` and le``tt``ers</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><code>monospace phrase</code> and le<code>tt</code>ers</p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Monospace Bold Italic</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">`*_monospace bold italic phrase_*` and le``**__tt__**``ers</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><code><strong><em>monospace bold italic phrase</em></strong></code> and le<code><strong><em>tt</em></strong></code>ers</p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Links</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">https://github.com/asciidoctor[Asciidoctor @ *GitHub*]</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="https://github.com/asciidoctor">Asciidoctor @ <strong>GitHub</strong></a></p></td>
			</tr>
			<tr>
			<th class="tableblock halign-left valign-top"><p class="tableblock">Mailto</p></th>
			<td class="tableblock halign-left valign-top"><p class="tableblock">brian.smith@numberfour.eu</p></td>
			<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="mailto:brian.smith@numberfour.eu">brian.smith@numberfour.eu</a></p></td>
			</tr>
			</tbody>
			</table>''',
			'''
			[cols="1,4,2"]
			|===
			h| Type h| Source h| Output
			h| Italic|+++_italic phrase_+++ | _italic phrase_
			h| Bold Phrase|+++*bold phrase*+++ | *bold phrase*
			h| Bold Letters |+++**b**old le**tt**ers+++ | **b**old le**tt**ers
			h| Bold Italic Phrase |+++*_bold italic phrase_*+++ | *_bold italic phrase_*
			h| Monospace |+++`monospace phrase` and le``tt``ers+++ | `monospace phrase` and le``tt``ers
			h| Monospace Bold Italic|+++`*_monospace bold italic phrase_*` and le``**__tt__**``ers+++ | `*_monospace bold italic phrase_*` and le``**__tt__**``ers
			h| Links |+++https://github.com/asciidoctor[Asciidoctor @ *GitHub*]+++ |  https://github.com/asciidoctor[Asciidoctor @ *GitHub*]
			h| Mailto |+++brian.smith@numberfour.eu+++ |  brian.smith@numberfour.eu
			|==='''
		)
	}
	
	@Test
	public def testInlineMacroEscapedWithInlinePass() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Hello!: nop:hello[]</p>
			</div>''',
			'''
			Hello!: +++nop:hello[]+++
			'''
		)
	}
	
	@Test
	public def testInlineMacroEscapedWithPassBlock() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Well, if this ain&#8217;t a pass block!</p>
			</div>
			Hello!: nop:hello[]
			<div class="paragraph">
			<p>Yup, it was! Did it work?!</p>
			</div>''',
			'''
			Well, if this ain't a pass block!
			
			++++
			Hello!: nop:hello[]
			++++
			
			Yup, it was! Did it work?!
			'''
		)
	}
	
	@Test
	@Ignore // this is expected to fail, as document variables don't properly survive our AST rewriting...
	public def testDocumentVariableReplacements() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>We should travel to Italy!</p>
			</div>
			<div class="paragraph">
			<p>And also to Spain.</p>
			</div>
			''',
			'''
			:country: Italy
			
			We should travel to {country}!
			
			:country: Spain
			
			And also to {country}.
			'''
		)
	}
}
