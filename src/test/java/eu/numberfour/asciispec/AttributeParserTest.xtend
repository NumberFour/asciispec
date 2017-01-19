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

import eu.numberfour.asciispec.ParseException
import java.util.Map
import org.junit.Assert
import org.junit.Test

/**
 * Test cases for {@link AttributeParser}.
 */
class AttributeParserTest {
	private def void parseAndAssert(Map<Object, Object> expected, String dataToParse) {
		try {
			Assert.assertEquals(expected, AttributeParser.parse(dataToParse));
		} catch (ParseException e) {
			Assert.fail("Unexpected exception while parsing '" + dataToParse + "': " + e);
		}
	}

	@Test
	public def void testEmptyStrings() {
		parseAndAssert(newHashMap(), "");
		parseAndAssert(newHashMap(), " ");
		parseAndAssert(newHashMap(), "   ");
		parseAndAssert(newHashMap(), "\t");
		parseAndAssert(newHashMap(), "\t \t");
		parseAndAssert(newHashMap(), "\n");
		parseAndAssert(newHashMap(), "\n\n\n\n\n");
		parseAndAssert(newHashMap(), "\n\n\n  \n\n");
		parseAndAssert(newHashMap(), "\n\n\t\t\n  \n\n");
	}
	
	@Test
	public def void testAnonymousArguments() {
		parseAndAssert(newHashMap("0" -> "asdf"), "asdf");
		parseAndAssert(newHashMap("0" -> "asdf"), " asdf   ");
		parseAndAssert(
			newHashMap(
				"0" -> "asdf", 
				"1" -> "fdsa"
			), 
			" asdf  , fdsa ");
		parseAndAssert(
			newHashMap(
				"0" -> "asdf", 
				"1" -> "1", 
				"2" -> "2", 
				"3" -> "3"
			), 
			" asdf  , 1,2,3 "
		);

		parseAndAssert(newHashMap("0" -> "asdf"), '''"asdf"''');
		parseAndAssert(newHashMap("0" -> " asdf   "), '''" asdf   "''');
		parseAndAssert(newHashMap("0" -> " asdf  , fdsa "), ''' " asdf  , fdsa "  ''');
		parseAndAssert(
			newHashMap(
				"0" -> " asdf", 
				"1" -> "fdsa "
			), 
			''' " asdf"  , "fdsa "  '''
		);
		parseAndAssert(
			newHashMap(
				"0" -> " asdf ", 
				"1" -> "1", 
				"2" -> "2", 
				"3" -> "3"
			), 
			'''" asdf " , 1,2, "3" '''
		);
	}
	
	@Test
	public def void testNamedArguments() {
		parseAndAssert(newHashMap("key" -> "value"), "key=value");
		parseAndAssert(newHashMap("key" -> "value and something"), "key=value and something");
		parseAndAssert(newHashMap("key" -> "value and something"), "key = value and something  ");
		parseAndAssert(newHashMap("key" -> "value"), " key  = value  ");
		parseAndAssert(newHashMap(
			"key" -> "value",
			"another" -> "secondvalue",
			"andanother" -> "thirdvalue"), 
		" key  = value , another=secondvalue,andanother=thirdvalue ");

		parseAndAssert(newHashMap("key" -> " value"), '''key=" value"''');
		parseAndAssert(newHashMap("key" -> "value"), ''' key  = "value"  ''');
	}
	
	@Test
	public def void testMixedArguments() {
		parseAndAssert(newHashMap(
			"key1" -> "value1",
			"0" -> "value2",
			"1" -> "value3",
			"key2" -> "value4"
		), '''key1=value1, value2, "value3", key2 = "value4"''')
	}
}
