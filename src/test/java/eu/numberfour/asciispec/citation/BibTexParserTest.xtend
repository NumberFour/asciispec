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
package eu.numberfour.asciispec.citation

import eu.numberfour.asciispec.citation.BibTexParser
import eu.numberfour.asciispec.citation.BibliographyEntry
import java.util.List
import java.util.Map
import org.junit.Assert
import org.junit.Test

/**
 * Test cases for {@link BibTexParser}.
 */
class BibTexParserTest {
	private def void parseAndAssert(String type, String citeKey, String data) {
		parseAndAssert(type, citeKey, newHashMap(), data);
	}

	private def void parseAndAssert(String type, String citeKey, Map<String, String> tags, String data) {
		parseAndAssert(newLinkedList(new BibliographyEntry(type, citeKey, tags)), data);
	}

	private def void parseAndAssert(List<BibliographyEntry> expected, String data) {
		try {
			val List<BibliographyEntry> actual = BibTexParser.parse(data, new IdentityBibTexTagProcessor());
			Assert.assertTrue("Expected " + expected.toString() + ", but got " + actual.toString(), BibliographyEntry.equals(expected, actual));
		} catch (Exception e) {
			Assert.fail(e.getMessage())
		}
	}

	@Test
	public def void parseEmptyString() {
		parseAndAssert(newLinkedList(), '''''');
	}
	
	@Test
	public def void parseBlankString() {
		parseAndAssert(
			newLinkedList(), 
			
			'''
			    
			     
			''');
	}
	
	@Test
	public def void parseCommentsOnly() {
		parseAndAssert(
			newLinkedList(),
			
			'''
			%% This is a comment
			
			% and another
			'''
		);
	}
	
	@Test
	public def void parseSingleEntryWithoutTags() {
		parseAndAssert(
			"article", "OMG99a", 
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a }
			
			% More comments here
			%%%% Booooooooooooooooring, but there's no newline here!''');
	}
	
	@Test
	public def void parseSingleEntryWithQuotedTags() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> "Jens von Pilgrim",
				"title" -> "My Disseration"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Author = "Jens von Pilgrim",
				TITLE="My Disseration"
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithBraceEnclosedTag() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> "Jens von Pilgrim"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Author = {Jens von Pilgrim}
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithReplaceableTagValue() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"month" -> "feb"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Month = feb
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithBraceLatexStringTagValue() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> "Jens {von} {{Pilgrim}}"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Author = {Jens {von} {{Pilgrim}}}
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithBraceLatexStringTagValueContaininedEscapedControlChars() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> '''Jen\s\ \{von\} \\{{Pilgrim}}'''
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Author = {Jen\s\ \{von\} \\{{Pilgrim}}}
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithMixedTags() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> "Jens {von} {{Pilgrim}}",
				"title" -> "My Disseration"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				Author = {Jens {von} {{Pilgrim}}},
				TITLE="My Disseration"
			}
		''');
	}
	
	@Test
	public def void parseSingleEntryWithMixedAndNumberTags() {
		parseAndAssert(
			"article", "OMG99a", newHashMap(
				"author" -> "Jens {von} {{Pilgrim}}",
				"title" -> "My Disseration",
				"year" -> "2005"
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				year=2005,
				Author = {Jens {von} {{Pilgrim}}},
				TITLE="My Disseration"
			}
		''');
	}
	
	@Test
	public def void parseMultipleEntries() {
		parseAndAssert(
			newLinkedList(
				new BibliographyEntry("article", "OMG99a", newHashMap(
				"author" -> "Jens {von} {{Pilgrim}}",
				"title" -> "My Disseration",
				"year" -> "2005"
				)),
				new BibliographyEntry("thesis", "DatMewsGuy", newHashMap(
				"author" -> "Marcus Mews",
				"title" -> "Refactoring PWNZ",
				"year" -> "2016",
				"comment" -> 
					'''
					This Request for Proposal (RFP) is one of a series of RFPs related to 
					developing the next major revision of the OMG Meta Object Facility 
					specification, which will be referred to as MOF 2.0. Some of the RFPs 
					pertain to specifying the technology neutral MOF itself, while others 
					pertain to mapping the MOF to specific implementation technologies. 
					This RFP addresses a technology neutral part of MOF and pertains to: 
					1. Queries on models. 
					2. Views on metamodels. 
					3. Transformations of models.'''
				))
			),
			
			'''
			% This is a comment
			
			% So is this
			
			@Article { OMG99a,
				year=2005,
				Author = {Jens {von} {{Pilgrim}}},
				TITLE="My Disseration"
			}
			@thesis{DatMewsGuy,
			author="Marcus Mews",title="Refactoring PWNZ",
			
			year =     2016,
			comment={This Request for Proposal (RFP) is one of a series of RFPs related to 
			developing the next major revision of the OMG Meta Object Facility 
			specification, which will be referred to as MOF 2.0. Some of the RFPs 
			pertain to specifying the technology neutral MOF itself, while others 
			pertain to mapping the MOF to specific implementation technologies. 
			This RFP addresses a technology neutral part of MOF and pertains to: 
			1. Queries on models. 
			2. Views on metamodels. 
			3. Transformations of models.}}
		''');
	}
	
	@Test
	public def testLoadExample1() {
		parseAndAssert(
			"manual", "OMG09a", newHashMap(
				"address" -> "Needham, MA",
				"author" -> "OMG",
				"booktitle" -> "Unified Modeling Language: Superstructure, Version 2.2",
				"date-added" -> "2016-08-19 12:37:55 +0000",
				"date-modified" -> "2016-08-19 12:37:55 +0000",
				"edition" -> "formal/2009-02-02",
				"importance" -> "5",
				"keywords" -> "UML",
				"organization" -> "Object Management Group",
				"printed" -> "Yes",
				"rating" -> "5",
				"title" -> "Unified Modeling Language: Superstructure, Version 2.2",
				"url" -> "http://www.omg.org/cgi-bin/doc?formal/2009-02-02",
				"year" -> "2009",
				"abstract" -> '''
				This Request for Proposal (RFP) is one of a series of RFPs related to 
				developing the next major revision of the OMG Meta Object Facility 
				specification, which will be referred to as MOF 2.0. Some of the RFPs 
				pertain to specifying the technology neutral MOF itself, while others 
				pertain to mapping the MOF to specific implementation technologies. 
				This RFP addresses a technology neutral part of MOF and pertains to: 
				1. Queries on models. 
				2. Views on metamodels. 
				3. Transformations of models. '''
			),
			'''
			%% This BibTeX bibliography file was created using BibDesk.
			%% http://bibdesk.sourceforge.net/
			
			
			%% Created for Jens von Pilgrim at 2016-08-19 14:38:09 +0200 
			
			
			%% Saved with string encoding Unicode (UTF-8) 
			
			
			
			@manual{OMG09a,
				Address = {Needham, MA},
				Author = {OMG},
				Booktitle = {{Unified Modeling Language: Superstructure, Version 2.2}},
				Date-Added = {2016-08-19 12:37:55 +0000},
				Date-Modified = {2016-08-19 12:37:55 +0000},
				Edition = {formal/2009-02-02},
				Importance = {5},
				Keywords = {UML},
				Organization = {Object Management Group},
				Printed = {Yes},
				Rating = {5},
				Title = {{Unified Modeling Language: Superstructure, Version 2.2}},
				Url = {http://www.omg.org/cgi-bin/doc?formal/2009-02-02},
				Year = {2009},
				Abstract = {This Request for Proposal (RFP) is one of a series of RFPs related to 
			developing the next major revision of the OMG Meta Object Facility 
			specification, which will be referred to as MOF 2.0. Some of the RFPs 
			pertain to specifying the technology neutral MOF itself, while others 
			pertain to mapping the MOF to specific implementation technologies. 
			This RFP addresses a technology neutral part of MOF and pertains to: 
			1. Queries on models. 
			2. Views on metamodels. 
			3. Transformations of models. }}'''
		);
	}
}
