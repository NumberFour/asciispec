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
package eu.numberfour.asciispec.sourceindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.junit.Assert;
import org.junit.Test;

import eu.numberfour.asciispec.ParseException;

/**
 * Test cases for {@link IndexFileParser}.
 */
@SuppressWarnings("javadoc")
public class IndexFileParserTest {

	private void parseAndAssert(List<String> expectedTQNs, String data) {
		try {
			List<IndexEntryInfo> expectedEntries = new ArrayList<>();
			for (String tqn : expectedTQNs) {
				IndexEntryInfo sie = IndexEntryInfoFactory.create(tqn);
				expectedEntries.add(sie);
			}

			SourceIndexDatabase parsedDB = IndexFileParser.parse(data);
			List<IndexEntryInfo> actualEntries = parsedDB.getAllEntries();

			boolean equal = true;
			equal = equal && expectedEntries.size() == actualEntries.size();
			equal = equal && expectedEntries.containsAll(actualEntries);

			String expectedString = Arrays.toString(expectedEntries.toArray());
			String actualString = Arrays.toString(actualEntries.toArray());
			Assert.assertTrue("Expected " + expectedString + ", but got " + actualString, equal);
		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void parseOneEntry() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#asString:64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				StructuredText#asString::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseNoElement() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText:64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				StructuredText::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseSpecialName1() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#asString:64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				\"StructuredText\"#asString::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseSpecialName2() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#asString:64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				\"StructuredText\"#\"asString\"::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseSpecialName3() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:\"Structuredö #^^Text\"#asString:64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				\"Structuredö #^^Text\"#asString::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseSpecialName4() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#\"as \\\" +* String\":64:24:40");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				StructuredText#\"as \\\" +* String\"::64::24::40\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseHasTrueFolder1() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredTextString:64:24:40:stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js-gen");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			StructuredText.n4js\n" +
				"				StructuredTextString::64::24::40::stdlib_api:packages:eu.numberfour.stdlib.format.api:src.n4js-gen\n"
				+ "";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

	@Test
	public void parseSmallIndexFile() {
		LinkedList<String> expectedTQNs = CollectionLiterals.newLinkedList(
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat:15:5:6",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat#format:20:7:23",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat#parse:23:24:40",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withPattern:16:41:57",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle:18:58:74",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormatEmphasis:6:75:76",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:DurationFormatEmphasis:51:5:6",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:DurationQuantityStyle:30:7:8",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:DurationStartAndRangeStyle:10:9:10",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat:60:11:12",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat#format:73:13:29",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat#parse:76:30:46",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@getQuantityFormat:67:47:63",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@getRangeFormat:65:64:80",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@getStartFormat:63:81:97",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@getStartWithQuantityFormat:71:98:114",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@getStartWithRangeFormat:69:115:131",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanFormat@withPattern:61:132:148",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat:83:149:150",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat#format:94:151:167",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat#parse:97:168:184",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@getQuantityFormat:88:185:201",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@getRangeFormat:86:202:218",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@getStartWithQuantityFormat:92:219:235",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@getStartWithRangeFormat:90:236:252",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@withPattern:84:253:269",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:Format:10:5:6",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:Format#format:21:7:17",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:Format#parse:35:18:28",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:ParseResult:38:29:30",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:ParseResult#errorMessage:41:31:41",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:ParseResult#isSuccess:39:42:52",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/Format.n4js:ParseResult#value:40:53:63",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/NumberFormat.n4js:NumberFormat:8:5:6",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/NumberFormat.n4js:NumberFormat#format:13:7:23",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/NumberFormat.n4js:NumberFormat#parse:16:24:40",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/NumberFormat.n4js:NumberFormat@withPattern:9:41:57",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/NumberFormat.n4js:NumberFormat@withStyle:11:58:74",

				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText:17:5:6",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#asN4ML:73:7:23",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#asString:64:24:40",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText#onAction:23:41:51",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText@fromN4ML:55:52:68",
				"stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/StructuredText.n4js:StructuredText@fromPlainText:38:69:85");

		String indexFileContent = "" +
				"stdlib_api#packages\n" +
				"	eu.numberfour.stdlib.format.api#src.n4js\n" +
				"		n4.format\n" +
				"			DateTimeFormat.n4js\n" +
				"				DateTimeFormat::15::5::6\n" +
				"				DateTimeFormat#format::20::7::23\n" +
				"				DateTimeFormat#parse::23::24::40\n" +
				"				DateTimeFormat@withPattern::16::41::57\n" +
				"				DateTimeFormat@withStyle::18::58::74\n" +
				"				DateTimeFormatEmphasis::6::75::76\n" +
				"			DurationFormats.n4js\n" +
				"				DurationFormatEmphasis::51::5::6\n" +
				"				DurationQuantityStyle::30::7::8\n" +
				"				DurationStartAndRangeStyle::10::9::10\n" +
				"				TimeSpanFormat::60::11::12\n" +
				"				TimeSpanFormat#format::73::13::29\n" +
				"				TimeSpanFormat#parse::76::30::46\n" +
				"				TimeSpanFormat@getQuantityFormat::67::47::63\n" +
				"				TimeSpanFormat@getRangeFormat::65::64::80\n" +
				"				TimeSpanFormat@getStartFormat::63::81::97\n" +
				"				TimeSpanFormat@getStartWithQuantityFormat::71::98::114\n" +
				"				TimeSpanFormat@getStartWithRangeFormat::69::115::131\n" +
				"				TimeSpanFormat@withPattern::61::132::148\n" +
				"				TimeSpanPatternFormat::83::149::150\n" +
				"				TimeSpanPatternFormat#format::94::151::167\n" +
				"				TimeSpanPatternFormat#parse::97::168::184\n" +
				"				TimeSpanPatternFormat@getQuantityFormat::88::185::201\n" +
				"				TimeSpanPatternFormat@getRangeFormat::86::202::218\n" +
				"				TimeSpanPatternFormat@getStartWithQuantityFormat::92::219::235\n" +
				"				TimeSpanPatternFormat@getStartWithRangeFormat::90::236::252\n" +
				"				TimeSpanPatternFormat@withPattern::84::253::269\n" +
				"			Format.n4js\n" +
				"				Format::10::5::6\n" +
				"				Format#format::21::7::17\n" +
				"				Format#parse::35::18::28\n" +
				"				ParseResult::38::29::30\n" +
				"				ParseResult#errorMessage::41::31::41\n" +
				"				ParseResult#isSuccess::39::42::52\n" +
				"				ParseResult#value::40::53::63\n" +
				"			NumberFormat.n4js\n" +
				"				NumberFormat::8::5::6\n" +
				"				NumberFormat#format::13::7::23\n" +
				"				NumberFormat#parse::16::24::40\n" +
				"				NumberFormat@withPattern::9::41::57\n" +
				"				NumberFormat@withStyle::11::58::74\n" +
				"			StructuredText.n4js\n" +
				"				StructuredText::17::5::6\n" +
				"				StructuredText#asN4ML::73::7::23\n" +
				"				StructuredText#asString::64::24::40\n" +
				"				StructuredText#onAction::23::41::51\n" +
				"				StructuredText@fromN4ML::55::52::68\n" +
				"				StructuredText@fromPlainText::38::69::85\n" +
				"";

		parseAndAssert(expectedTQNs, indexFileContent);
	}

}
