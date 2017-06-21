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
import eu.numberfour.asciispec.processors.InlineTaskLinkProcessor.TaskStatus
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Before
import org.junit.Test

/**
 * Test cases for {@link InlineTaskLinkProcessor}.
 */
class InlineTaskProcessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		new InlineTaskLinkExtension().register(doc);
	}

	private def getConfig() '''
		:task_def_GH-: GitHub;IDE Bugs;https://github.com/NumberFour/N4JS/issues/{TASK_ID};github;GH-{TASK_ID}
		:task_def_IDE-: Jira;IDE Backlog;https://jira.numberfour.eu/browse/IDE-{TASK_ID};tasks;IDE-{TASK_ID}
		
	'''

	private def getConfigWithTaskStatusFile(String taskStatusFile) {
		val Path relPath = Paths.get("src/test/resources/data/inline_task_status_file", taskStatusFile);
		val Path absPath = relPath.toAbsolutePath();

		return '''
			:task_def_GH-: GitHub;IDE Bugs;https://github.com/NumberFour/N4JS/issues/{TASK_ID};github;GH-{TASK_ID};«absPath.toUri().toURL()»
			
		'''
	}

	@Test
	public def void testMissingConfiguration() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			Missing task configuration
			</div>
			</div>''',
			'''
			Lorem ipsum dolor sit task::GM-40[] amet, consectetur adipiscing elit.''',
			"asciispec  : ERROR: Missing task configuration"
		);
	}

	@Test
	public def void testMissingInvalidConfiguration() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			Invalid repository configuration string: 'GitHub;IDE Bugs;https://github.com/NumberFour/N4JS/issues/{TASK_ID}'
			</div>
			</div>''',
			'''
			:task_def_GH-: GitHub;IDE Bugs;https://github.com/NumberFour/N4JS/issues/{TASK_ID}
			Lorem ipsum dolor sit task::GM-40[] amet, consectetur adipiscing elit.''',
			"asciispec  : ERROR: Invalid repository configuration string: 'GitHub;IDE Bugs;https://github.com/NumberFour/N4JS/issues/{TASK_ID}'"
		);
	}

	@Test
	public def void testUnknownRepository() {
		convertStringAndAssertErrorContains(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			Unknown task repository for task 'GM-40'
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::GM-40[] amet, consectetur adipiscing elit.''',
			"asciispec  : ERROR: Unknown task repository for task 'GM-40'"
		);
	}

	@Test
	public def void testProcessGitHubTask() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/40" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-40</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::GH-40[] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessJiraTask() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>IDE-2288</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::IDE-2288[] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessJiraTaskWithLabel() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::IDE-2288[title="My Title"] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessJiraTaskWithLabelNoQM() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::IDE-2288[title=My Title] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessJiraTaskWithLabelShorthand() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::IDE-2288[My Title] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessJiraTaskWithLabelShorthandQM() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::IDE-2288["My Title"] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessMultipleTasks() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit  amet, consectetur  adipiscing elit.</p>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/40" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-40</a>
			<a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>IDE-2288</a>
			</div>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task::GH-40[] amet, consectetur task::IDE-2288[] adipiscing elit.'''
		);
	}

	@Test
	public def void testProcessTaskInList() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is a list:</p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>First item</p>
			</li>
			<li>
			<p>Something </p>
			</li>
			<li>
			<p>Third item</p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/456" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-456</a>
			</div>
			</div>
			<div class="paragraph">
			<p>You&#8217;re gonna fix this! End of!</p>
			</div>''',
			'''
			«config»
			This is a list:
			
			* First item
			* Something task::gh-456[]
			* Third item
			
			You're gonna fix this! End of!'''
		);
	}

	@Test
	public def void testProcessTaskInNestedList() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is a list:</p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>Item 1</p>
			<div class="ulist">
			<ul>
			<li>
			<p>Item 1.1</p>
			</li>
			<li>
			<p>Item 1.2: The first task! </p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/456" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-456</a>
			</div>
			</div>
			</li>
			<li>
			<p>Item 2</p>
			</li>
			<li>
			<p>Item 3: Another task! </p>
			<div class="ulist">
			<ul>
			<li>
			<p>Item 3.1: Another nested list.</p>
			</li>
			</ul>
			</div>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/567" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-567</a>
			</div>
			</div>
			<div class="paragraph">
			<p>You&#8217;re gonna fix this! End of!</p>
			</div>''',
			'''
			«config»
			This is a list:
			
			* Item 1
			** Item 1.1
			** Item 1.2: The first task! task::gh-456[]
			* Item 2
			* Item 3: Another task! task::gh-567[]
			** Item 3.1: Another nested list.
			
			You're gonna fix this! End of!'''
		);
	}

	@Test
	public def void testProcessTaskInTable() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is a table:</p>
			</div>
			<table class="tableblock frame-all grid-all spread">
			<colgroup>
			<col style="width: 100%;">
			</colgroup>
			<tbody>
			<tr>
			<td class="tableblock halign-left valign-top"><div><div class="paragraph">
			<p>Here&#8217;s some task! </p>
			</div></div></td>
			</tr>
			</tbody>
			</table>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/456" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-456</a>
			</div>
			</div>
			<div class="paragraph">
			<p>You&#8217;re gonna fix this! End of!</p>
			</div>''',
			'''
			«config»
			This is a table:
			
			|===
			a| Here's some task! task::gh-456[]
			|===
			
			You're gonna fix this! End of!'''
		);
	}

	@Test
	public def void testProcessTaskInListNestedInTable() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>This is a table:</p>
			</div>
			<table class="tableblock frame-all grid-all spread">
			<colgroup>
			<col style="width: 100%;">
			</colgroup>
			<tbody>
			<tr>
			<td class="tableblock halign-left valign-top"><div><div class="paragraph">
			<p>Here&#8217;s some tasks </p>
			</div>
			<div class="ulist">
			<ul>
			<li>
			<p>More: </p>
			</li>
			<li>
			<p>One more: </p>
			</li>
			<li>
			<p>Even more: </p>
			</li>
			</ul>
			</div></div></td>
			</tr>
			</tbody>
			</table>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/456" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-456</a>
			<a href="https://github.com/NumberFour/N4JS/issues/123" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-123</a>
			<a href="https://github.com/NumberFour/N4JS/issues/234" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-234</a>
			<a href="https://github.com/NumberFour/N4JS/issues/345" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-345</a>
			</div>
			</div>
			<div class="paragraph">
			<p>You&#8217;re gonna fix this! End of!</p>
			</div>''',
			'''
			«config»
			This is a table:
			
			[cols="1"]
			|===
			
			a| Here's some tasks task::gh-456[]
			
			* More: task::GH-123[]
			* One more: task::GH-234[]
			* Even more: task::GH-345[]
			
			|===
			
			You're gonna fix this! End of!'''
		);
	}

	@Test
	public def void testProcessComplexDocumentWithFailure() {
		convert(
		'''
			«config»
			= Definition Block Documentation
			
			Usage::
			[source,adoc]
			----
			.definitionTitle
			[def]
			--
			The content of the definition
			--
			----
			
			Attributes::
			* *definitionTitle:* An anchor is derived from the definition title and embedded at the beginning of the rendered output.
			
			Description:: The `definition block` is used to list definitions with as minimal additional formatting required by the author.
			All lists within the definition block are formatted as constraints of that definition.
			
			Example::
			
			[source,adoc]
			-----
			.Definition Site Structural Typing
			[def]
			--
			If a type T is declared as structural at its definition, _T.defStructural_ is true.
			
			1. The structurally defined type cannot be used on the right hand side of the `instanceof`
			2. A type X is a subtype of a structurally defined type T...
			
			Furthermore...
			--
			-----
			
			The above source will create the following output:
			
			---
			
			ifdef::env-github[]
			[discrete]
			=== Definition Site Structural Typing
			
			If a type T is declared as structural at its definition, _T.defStructural_ is true.
			
			Constraints (Definition Site Structural Typing):
			
			1. The structurally defined type cannot be used on the right-hand side of the `instanceof`...
			2. A type X is a subtype of a structurally defined type T...
			
			Furthermore...
			---
			endif::[]
			
			ifndef::env-github[]
			tag::example[]
			.Definition Site Structural Typing
			[def,title=something]
			--
			If a type T is declared as structural at its definition, _T.defStructural_ is true.
			
			1. The structurally defined type cannot be used on the right hand side of the `instanceof`
			2. A type X is a subtype of a structurally defined type T...
			
			Furthermore...
			--
			end::example[]
			endif::[]
		''');
	}

	@Test
	public def void testTaskWithValidStatusFile() {
		convertAndAssert(
			'''
			<div class="ulist">
			<ul>
			<li>
			<p>More: </p>
			</li>
			<li>
			<p>One more: </p>
			</li>
			<li>
			<p>Even more: </p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/123" class="«TaskStatus.OPEN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-123</a>
			<a href="https://github.com/NumberFour/N4JS/issues/234" class="«TaskStatus.CLOSED.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-234</a>
			<a href="https://github.com/NumberFour/N4JS/issues/345" class="«TaskStatus.OPEN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-345</a>
			</div>
			</div>''',
			'''
				«getConfigWithTaskStatusFile("status_valid.txt")»
				* More: task::GH-123[]
				* One more: task::GH-234[]
				* Even more: task::GH-345[]
			'''
		)
	}

	@Test
	public def void testTaskWithInvalidStatusFile() {
		convertStringAndAssertErrorContains(
			'''
			<div class="ulist">
			<ul>
			<li>
			<p>More: </p>
			</li>
			<li>
			<p>One more: </p>
			</li>
			<li>
			<p>Even more: </p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/123" class="«TaskStatus.OPEN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-123</a>
			<a href="https://github.com/NumberFour/N4JS/issues/234" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-234</a>
			<a href="https://github.com/NumberFour/N4JS/issues/345" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-345</a>
			</div>
			</div>''',
			'''
				«getConfigWithTaskStatusFile("status_invalid.txt")»
				* More: task::GH-123[]
				* One more: task::GH-234[]
				* Even more: task::GH-345[]
			''',
			"asciispec  : WARN: Malformed task status entry in line 2 of task status file"
		)
	}

	@Test
	public def void testTaskWithMissingStatusFile() {
		convertAndAssert(
			'''
			<div class="ulist">
			<ul>
			<li>
			<p>More: </p>
			</li>
			<li>
			<p>One more: </p>
			</li>
			<li>
			<p>Even more: </p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/123" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-123</a>
			<a href="https://github.com/NumberFour/N4JS/issues/234" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-234</a>
			<a href="https://github.com/NumberFour/N4JS/issues/345" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Bugs"><span class="image"><img src="github" alt=""></span>GH-345</a>
			</div>
			</div>''',
			'''
				«getConfigWithTaskStatusFile("status_missing.txt")»
				* More: task::GH-123[]
				* One more: task::GH-234[]
				* Even more: task::GH-345[]
			'''
		)
	}

	@Test
	public def void testInlineTask() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit <a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>IDE-2288</a> amet, consectetur adipiscing elit.</p>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task:IDE-2288[] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testTaskWithLabel() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit <a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a> amet, consectetur adipiscing elit.</p>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task:IDE-2288[title="My Title"] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testTaskWithLabelShorthand() {
		convertAndAssert(
			'''
			<div class="paragraph">
			<p>Lorem ipsum dolor sit <a href="https://jira.numberfour.eu/browse/IDE-2288" class="«TaskStatus.UNKNOWN.getRole()»" title="IDE Backlog"><span class="image"><img src="tasks" alt=""></span>My Title</a> amet, consectetur adipiscing elit.</p>
			</div>''',
			'''
			«config»
			Lorem ipsum dolor sit task:IDE-2288["My Title"] amet, consectetur adipiscing elit.'''
		);
	}

	@Test
	public def void testTaskWithTitle() {
		convertAndAssert(
			'''
			<div class="ulist">
			<ul>
			<li>
			<p>More: </p>
			</li>
			<li>
			<p>One more: <a href="https://github.com/NumberFour/N4JS/issues/234" class="«TaskStatus.CLOSED.getRole()»" title="IDE Bugs: As a bee I need some flowers with blossoms"><span class="image"><img src="github" alt=""></span>Mice</a></p>
			</li>
			<li>
			<p>Even more: <a href="https://github.com/NumberFour/N4JS/issues/345" class="«TaskStatus.OPEN.getRole()»" title="IDE Bugs: As a texter I need these characters: colons, etc."><span class="image"><img src="github" alt=""></span>GH-345</a></p>
			</li>
			</ul>
			</div>
			<div class="sidebarblock">
			<div class="content">
			<a href="https://github.com/NumberFour/N4JS/issues/123" class="«TaskStatus.OPEN.getRole()»" title="IDE Bugs: As a developer I need a fast computer"><span class="image"><img src="github" alt=""></span>Developers</a>
			</div>
			</div>''',
			'''
			«getConfigWithTaskStatusFile("status_valid_with_title.txt")»
			* More: task::GH-123[Developers]
			* One more: task:GH-234[Mice]
			* Even more: task:GH-345[]'''
		);
	}
}
