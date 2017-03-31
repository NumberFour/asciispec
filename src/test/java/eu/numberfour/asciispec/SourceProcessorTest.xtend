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
package eu.numberfour.asciispec

import java.util.Iterator
import java.util.LinkedList
import java.util.List
import java.util.function.BiFunction
import org.junit.Test

import static org.junit.Assert.*

/**
 *
 */
class SourceProcessorTest {
	private static class MockTransform implements BiFunction<String, Integer, List<String>> {
		private static class Expectation {
			private String parameter;
			private LinkedList<String> result;

			public new(String parameter, LinkedList<String> result) {
				this.parameter = parameter;
				this.result = result;
			}

			def LinkedList<String> assertAndReturnResult(String str) {
				assertEquals(parameter, str);
				return result;
			}

			override toString() {
				var StringBuilder str = new StringBuilder();
				str.append("'").append(parameter).append("' -> ").append(result);
				return str.toString();
			}
		}

		private List<Expectation> expectations = newLinkedList();
		private Iterator<Expectation> currentExpectation;

		public new() {
			currentExpectation = expectations.iterator();
		}

		public def MockTransform expect(String parameter, String... result) {
			return expect(parameter, newLinkedList(result));
		}

		public def MockTransform expect(String parameter, LinkedList<String> result) {
			expectations.add(new Expectation(parameter, result));
			currentExpectation = expectations.iterator();
			return this;
		}

		public def void assertAllExpectationsMatched() {
			if (currentExpectation.hasNext()) {
				var StringBuilder message = new StringBuilder();
				message.append("Unmatched expectations:\n");
				while (currentExpectation.hasNext())
					message.append("   ").append(currentExpectation.next()).append("\n");
				fail(message.toString());
			}
		}

		override def LinkedList<String> apply(String str, Integer lineNumber) {
			assertTrue("Unexpected call with parameter '" + str + "'", currentExpectation.hasNext());

			val Expectation expectation = currentExpectation.next();
			return expectation.assertAndReturnResult(str);
		}
	}

	private def void processAndAssert(String expected, String text, MockTransform transform) {
		val SourceProcessor processor = new SourceProcessor(transform);

		val List<String> expectedLines = normalize(newLinkedList(expected.split("\\n", -1)));
		val List<String> textLines = newLinkedList(text.split("\\n", -1));
		val List<String> result = normalize(processor.process(textLines));
		assertEquals(expectedLines, result);

		transform.assertAllExpectationsMatched();
	}

	private static def List<String> normalize(List<String> list) {
		if (list.isEmpty())
			return newLinkedList("");
		return list;
	}

	@Test
	def void testEmptyString() {
		processAndAssert(
			"",
			"",
			new MockTransform().expect("", "")
		);
	}

	@Test
	def void testEmptyLine() {
		processAndAssert(
			"\n",
			"\n",
			new MockTransform().expect("", "").expect("", "")
		);
	}

	@Test
	def void testTransformOneLine() {
		processAndAssert(
			"transformed",
			" asdf   asdf",
			new MockTransform().expect(" asdf   asdf", "transformed")
		);
	}

	@Test
	def void testTransformOneLineToTwo() {
		processAndAssert(
			'''
			transformed1
			transformed2''',
			''' asdf   asdf''',
			new MockTransform().expect(" asdf   asdf", "transformed1", "transformed2")
		);
	}

	@Test
	def void testTransformOneLineToNothing() {
		processAndAssert(
			'''''',
			''' asdf   asdf''',
			new MockTransform().expect(" asdf   asdf")
		);
	}

	@Test
	def void testTransformWithMultiLineComments() {
		processAndAssert(
			'''
			first
			////
			This should be ignored
			so should this
			and this
			////
			second
			////
			This, on the other hand...
			should be ignored as well!
			////
			third
			fourth''',
			'''
			asdf   asdf
			////
			This should be ignored
			so should this
			and this
			////
			but not this
			////
			This, on the other hand...
			should be ignored as well!
			////
			Not this, however. Not this.''',
			new MockTransform()
				.expect("asdf   asdf", "first")
				.expect("but not this", "second")
				.expect("Not this, however. Not this.", "third", "fourth")
		);
	}

	@Test
	def void testTransformWithInlineComments() {
		processAndAssert(
			'''
			first
			// This should also be ignored
			// Just like this
			second
			third
			fourth
			fifth''',
			'''
			This should be processed// but not this
			// This should also be ignored
			// Just like this
			But this must again be processed into multiple lines
			And this too, with a trailing // comment!''',
			new MockTransform()
				.expect("This should be processed// but not this", "first")
				.expect("But this must again be processed into multiple lines", "second", "third")
				.expect("And this too, with a trailing // comment!", "fourth", "fifth")
		);
	}

	@Test
	def void testTransformPassBlocks() {
		processAndAssert(
			'''
			first
			++++
			This should be ignored
			so should this
			and this
			++++
			second
			++++
			This, on the other hand...
			should be ignored as well!
			++++
			third
			fourth''',
			'''
			asdf   asdf
			++++
			This should be ignored
			so should this
			and this
			++++
			but not this
			++++
			This, on the other hand...
			should be ignored as well!
			++++
			Not this, however. Not this.''',
			new MockTransform()
				.expect("asdf   asdf", "first")
				.expect("but not this", "second")
				.expect("Not this, however. Not this.", "third", "fourth")
		);
	}

	@Test
	def void testTransformWithInlinePass() {
		processAndAssert(
			'''
			first+++ but not this+++
			+++This should also be ignored+++
			+++Just like this++++++ignored again+++
			second
			third
			fourth
			fifth+++pass+++''',
			'''
			This should be processed+++ but not this+++
			+++This should also be ignored+++
			+++Just like this+++not this again!+++ignored again+++
			But this must again be processed into multiple lines
			And this too, with a trailing +++pass+++''',
			new MockTransform()
				.expect("This should be processed", "first")
				.expect("not this again!")
				.expect("But this must again be processed into multiple lines", "second", "third")
				.expect("And this too, with a trailing ", "fourth", "fifth")
		);
	}

	@Test
	def void testTransformWithMixedInlinePasses() {
		processAndAssert(
			'''
			first pass:asdf[but not this]
			+++This should also be ignored+++
			pass:[]+++ignored again+++
			second
			third
			fourth
			fifthpass:[huhu]''',
			'''
			This should be processed pass:asdf[but not this]
			+++This should also be ignored+++
			pass:[]not this again!+++ignored again+++
			But this must again be processed into multiple lines
			And this too, with a trailing pass:[huhu]''',
			new MockTransform()
				.expect("This should be processed ", "first ")
				.expect("not this again!")
				.expect("But this must again be processed into multiple lines", "second", "third")
				.expect("And this too, with a trailing ", "fourth", "fifth")
		);
	}

	@Test
	def void testTransformWithSourceBlockUsingBackticks() {
		processAndAssert(
			'''
			Transformed first line
			Transformed second line
			```
					A wild source block appears!
			```
			Transformed last line''',
			'''
			This is the first line
			```
					A wild source block appears!
			```
			But it's over now!''',
			new MockTransform()
				.expect("This is the first line", "Transformed first line", "Transformed second line")
				.expect("But it's over now!", "Transformed last line")
		);
	}

	@Test
	def void testTransformWithParameterizedSourceBlockUsingBackticks() {
		processAndAssert(
			'''
			Transformed first line
			Transformed second line
			```bash
					A wild source block appears!
			```
			Transformed last line''',
			'''
			This is the first line
			```bash
					A wild source block appears!
			```
			But it's over now!''',
			new MockTransform()
				.expect("This is the first line", "Transformed first line", "Transformed second line")
				.expect("But it's over now!", "Transformed last line")
		);
	}

	@Test
	def void testTransformWithSourceBlock() {
		processAndAssert(
			'''
			Transformed first line
			Transformed second line
			[source]
			----
					A wild source block appears!
			----
			Transformed last line''',
			'''
			This is the first line
			[source]
			----
					A wild source block appears!
			----
			But it's over now!''',
			new MockTransform()
				.expect("This is the first line", "Transformed first line", "Transformed second line")
				.expect("But it's over now!", "Transformed last line")
		);
	}

	@Test
	def void testTransformWithParameterizedSourceBlock() {
		processAndAssert(
			'''
			Transformed first line
			Transformed second line
			[source,language=javascript]
			----
					A wild source block appears!
			----
			Transformed last line''',
			'''
			This is the first line
			[source,language=javascript]
			----
					A wild source block appears!
			----
			But it's over now!''',
			new MockTransform()
				.expect("This is the first line", "Transformed first line", "Transformed second line")
				.expect("But it's over now!", "Transformed last line")
		);
	}
}
