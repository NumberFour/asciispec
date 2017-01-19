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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.processors.SourceLinkPreprocessor;

/**
 * Test cases for {@link IndexFileParser}.
 */
@SuppressWarnings("javadoc")
public class SourceIndexDatabaseTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private void parseAndAssert(String expectedTQN, String srcLinkStr)
			throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {

		try {
			IndexEntryInfo expEntry = IndexEntryInfoFactory.create(expectedTQN);
			String expectedCompletePQN = expEntry.toPQN();

			SourceIndexDatabase db = getDB();

			Matcher srclnkMatcher = SourceLinkPreprocessor.SRC_LINK_PATTERN.matcher(srcLinkStr);
			assertTrue("Source link not found in: " + srcLinkStr, srclnkMatcher.find());
			String pqn = srclnkMatcher.group("PQN");
			List<String> pqnStack = PQNParser.parse(pqn);
			IndexEntryInfo entry = db.getEntry(pqnStack);

			String actualCompletePQN = entry.toPQN();
			assertEquals(expectedCompletePQN, actualCompletePQN);

		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void parseSimpleEntry1() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DurationFormats.n4js:TimeSpanPatternFormat@getStartWithQuantityFormat";
		String scrLink = "srclnk:getStartWithQuantityFormat[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry2() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry3() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry4() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry5() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry6() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry7() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry8() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:src/n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry9() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry10() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseSimpleEntry11() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle";
		String scrLink = "srclnk:stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/DateTimeFormat:DateTimeFormat@withStyle[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseModuleFunction1() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.n4js.base.api:src/n4js:n4/lang/IterableExt.n4js:first";
		String scrLink = "srclnk:++stdlib_api:packages:eu.numberfour.n4js.base.api:src/n4js/n4/lang/IterableExt:first++[LABEL]";
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseUnknownEntry() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle:64:24:40";
		String scrLink = "srclnk:DateXY@withStyle[LABEL]";
		exception.expect(NotInSourceIndexExcpetion.class);
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseAmbiguousPQN1() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle:64:24:40";
		String scrLink = "srclnk:withPattern[LABEL]";
		exception.expect(AmbiguousPQNExcpetion.class);
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	@Test
	public void parseAmbiguousPQN2() throws NotInSourceIndexExcpetion, AmbiguousPQNExcpetion {
		String expectedCompleteTQN = "stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js:n4/format/DateTimeFormat.n4js:DateTimeFormat@withStyle:64:24:40";
		String scrLink = "srclnk:@withPattern[LABEL]";
		exception.expect(AmbiguousPQNExcpetion.class);
		parseAndAssert(expectedCompleteTQN, scrLink);
	}

	private SourceIndexDatabase getDB() throws ParseException, IOException {
		Path pathToFile = Paths.get("src","test","resources", "data", "docu", "gen_adoc", "index.idx");
		List<String> lines = Files.readAllLines(pathToFile);
		String indexFileContent = String.join("\n", lines);

		SourceIndexDatabase parsedDB = IndexFileParser.parse(indexFileContent);
		return parsedDB;
	}

}
