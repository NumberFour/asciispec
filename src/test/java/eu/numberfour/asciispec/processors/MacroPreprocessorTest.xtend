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
package eu.numberfour.asciispec.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;
import org.junit.Before;
import org.junit.Test;

import eu.numberfour.asciispec.AsciidoctorTest;
import eu.numberfour.asciispec.processors.MacroPreprocessorTest.TestPreprocessorImpl

public class MacroPreprocessorTest extends AsciidoctorTest {

	@Before
	public def void registerExtensions() {
		new GenericExtension().register(doc);
	}

	public override void convertAndAssert(CharSequence expectedOutput, String input){
		super.convertAndAssert("<div class=\"paragraph\">\n<p>"+expectedOutput+"</p>\n</div>",input);
	}
	
	public override void convertStringAndAssertErrorContains(CharSequence expectedOutput, String input,String consoleError){
		super.convertStringAndAssertErrorContains("<div class=\"paragraph\">\n<p>"+expectedOutput+"</p>\n</div>",input, consoleError);
	}
	
	static class TestPreprocessorImpl extends MacroPreprocessor<String> {

		private final String KEY1 = "k1";
		private final String KEY2 = "k2";
		private final String KEY3 = "k3";
		private final String KEY4 = "error";
		private final String KEY5 = "warn";

		private final Pattern PATTERN1 = Pattern.compile(KEY1 + "\\[\\]");
		private final Pattern PATTERN2 = Pattern.compile(KEY2 + "\\[\\]");
		private final Pattern PATTERN3 = Pattern.compile(KEY3 + "\\[\\]");
		private final Pattern PATTERN4 = Pattern.compile(KEY4 + "\\[\\]");
		private final Pattern PATTERN5 = Pattern.compile(KEY5 + "\\[\\]");

		protected override boolean init(Document document) {
			registerPattern(KEY1, PATTERN1);
			registerPattern(KEY2, PATTERN2);
			registerPattern(KEY3, PATTERN3);
			registerPattern(KEY4, PATTERN4);
			registerPattern(KEY5, PATTERN5);
			return true;
		}

		protected override String processMatch(Document document, String key, Matcher matcher) {
			val result = matcher.group();
			switch (key) {
			case KEY1:
				return "A"
			case KEY2:
				return "BBBBBB"
			case KEY3:
				return "macro:test[]"
			case KEY4:
				return error(document,"bad bad error.")
			case KEY5:
				return warn(document,"just a warning, for now.")
			}
			return result;
		}
	}
	
	static class GenericExtension extends ProcessorExtension {
		protected override void register(JavaExtensionRegistry registry) {
			registry.preprocessor(TestPreprocessorImpl);
		}
	}
	
	@Test
	public def void testBasicDetectionAndConversion() {
		convertAndAssert("A", "k1[]");
		convertAndAssert("BBBBBB", "k2[]");
		convertAndAssert("macro:test[]", "k3[]");
	}
	
	@Test
	public def void testMultiplePatternsOfSameKind() {
		convertAndAssert("A A A", "k1[] k1[] k1[]");
	}
	
	@Test
	public def void testMultiplePatternsOfDifferentKindWithSteadyOrder() {
		convertAndAssert("A BBBBBB macro:test[]", "k1[] k2[] k3[]");
	}
	
	@Test
	public def void testMultiplePatternsOfDifferentKindWithRandomOrder() {
		convertAndAssert("A BBBBBB macro:test[] A macro:test[] BBBBBB", "k1[] k2[] k3[] k1[] k3[] k2[]");
	}
	
	@Test
	public def void testMultiplePatternsOfDifferentKindWithRandomOrderAndTextInbetween() {
		convertAndAssert("A text BBBBBB text text macro:test[] A texttext macro:test[] BBBBBB text", "k1[] text k2[] text text k3[] k1[] texttext k3[] k2[] text");
	}
	
	@Test
	public def void testErrorOutput() {
		convertStringAndAssertErrorContains(
		"<mark>[Error: bad bad error.]</mark>", 
		"error[]",
		"asciispec  : ERROR: line 1: bad bad error.");
	}
	
	@Test
	public def void testWarnOutput() {
		convertStringAndAssertErrorContains(
		"<mark>[Warn: just a warning, for now.]</mark>",
		"warn[]",
		"asciispec  : WARN: line 1: just a warning, for now.");
	}
	
	@Test
	public def void testErrorOutputLineNumber() {
		convertStringAndAssertErrorContains(
		"<mark>[Error: bad bad error.]</mark>", 
		"\n\n\nerror[]",
		"asciispec  : ERROR: line 4: bad bad error.");
	}
}
