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
package eu.numberfour.asciispec.math

import java.io.IOException
import org.junit.Assert
import org.junit.Test
import uk.ac.ed.ph.snuggletex.SnuggleEngine
import uk.ac.ed.ph.snuggletex.SnuggleInput
import uk.ac.ed.ph.snuggletex.SnuggleSession
import uk.ac.ed.ph.snuggletex.SnuggleSnapshot

/**
 * 
 */
class SnuggleTest {
	private def convertAndAssertBlock(CharSequence expected, CharSequence expression) {
		convertAndAssert(expected, "\\[" + expression + "\\]", new SnuggleEngine().createSession());
	}

	private def convertAndAssertBlock(CharSequence expected, CharSequence expression, SnuggleSession session) {
		convertAndAssert(expected, "\\[" + expression + "\\]", session);
	}

	private def convertAndAssert(CharSequence expected, CharSequence expression, SnuggleSession session) {
		try {
			val SnuggleInput input = new SnuggleInput(expression.toString());
			session.parseInput(input);

			Assert.assertEquals(expected.toString(), session.buildXMLString());
		} catch (IOException e) {
			Assert.fail(e.toString());
		}
	}
	
	@Test
	def void testNewCommand() {
		convertAndAssertBlock(
			'''
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>=</mo><msup><mi>X</mi><mi>y</mi></msup></math>''',
			'''
			\newcommand{\testcmd}{X^{y}}
			
			x = \testcmd
			'''
		)
	}
	
	@Test
	def void testParameterizedCommand() {
		convertAndAssertBlock(
			'''
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>=</mo><msup><mi>X</mi><mi>y</mi></msup></math>''',
			'''
			\newcommand{\testcmd}[1]{X^{#1}}
			
			x = \testcmd{y}
			'''
		)
	}
	
	@Test
	def void testSessionSnapshotWithCommands() throws IOException {
		val SnuggleEngine engine = new SnuggleEngine();
		val SnuggleSession session = engine.createSession();
		
		session.parseInput(new SnuggleInput(
			'''
			\newcommand{\testcmd}[1]{X^{#1}}
			'''
		));
		
		val SnuggleSnapshot snapshot = session.createSnapshot();
		
		convertAndAssertBlock(
			'''
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>=</mo><msup><mi>X</mi><mi>y</mi></msup></math>''',
			'''
			x = \testcmd{y}
			''',
			snapshot.createSession()
		)
		
		convertAndAssertBlock(
			'''
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>x</mi><mo>=</mo><msup><mi>X</mi><mi>z</mi></msup></math>''',
			'''
			x = \testcmd{z}
			''',
			snapshot.createSession()
		)
	}
	
	@Test
	def void testNestedCommands() throws IOException {
		val SnuggleEngine engine = new SnuggleEngine();
		val SnuggleSession session = engine.createSession();
		
		session.parseInput(new SnuggleInput(
			'''
			\newcommand{\typeEnv}{\Gamma}
			\newcommand{\entails}{\vdash}
			\newcommand{\tee}{\typeEnv \entails }
			'''
		));
		
		val SnuggleSnapshot snapshot = session.createSnapshot();
		
		convertAndAssertBlock(
			'''
			<math xmlns="http://www.w3.org/1998/Math/MathML" display="block"><mi>Γ</mi><mo>⊢</mo><mfenced close=")" open="("><mi>x</mi></mfenced></math>''',
			'''
			\tee(x)
			''',
			snapshot.createSession()
		)
	}
}
