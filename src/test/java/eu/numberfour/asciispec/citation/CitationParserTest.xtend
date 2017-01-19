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

import eu.numberfour.asciispec.ParseException
import java.util.List
import org.junit.Assert
import org.junit.Test

/**
 * Test cases for {@link CitationParser}
 */
class CitationParserTest {
	private def void parseAndAssert(List<CitationReference> expected, String dataToParse) {
		try {
			Assert.assertEquals(expected, CitationParser.parse(dataToParse));
		} catch (ParseException e) {
			Assert.fail("Unexpected exception while parsing '" + dataToParse + "': " + e);
		}
	}

	private def void parseAndFail(String dataToParse) {
		try {
			CitationParser.parse(dataToParse);
			Assert.fail("Expected exception while parsing '" + dataToParse + "'");
		} catch (ParseException e) {}
	}

	@Test
	public def void testEmptyStrings() {
		parseAndAssert(newLinkedList(), "");
		parseAndAssert(newLinkedList(), " ");
		parseAndAssert(newLinkedList(), "   ");
		parseAndAssert(newLinkedList(), "\t");
		parseAndAssert(newLinkedList(), "\t \t");
		parseAndAssert(newLinkedList(), "\n");
		parseAndAssert(newLinkedList(), "\n\n\n\n\n");
		parseAndAssert(newLinkedList(), "\n\n\n  \n\n");
		parseAndAssert(newLinkedList(), "\n\n\t\t\n  \n\n");
	}
	
	@Test
	public def void testOneCitationWithoutPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a")
			),
			'''OMG99a'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a")
			),
			'''     OMG99a'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a")
			),
			'''OMG99a    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a")
			),
			'''   OMG99a    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a")
			),
			'''   OMG99a (  )    '''
		);
		
		parseAndFail('''OMG99a,''');
		parseAndFail(''',OMG99a''');
	}
	
	@Test
	public def void testOneCitationWithOnePage() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", "1")
			),
			'''OMG99a(1)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", "1")
			),
			'''OMG99a  (1)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", "1")
			),
			'''OMG99a    (   1   )'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", "1")
			),
			'''   OMG99a    ( 1)    '''
		);
		
		parseAndFail('''OMG99a,(1)''');
		parseAndFail('''OMG99a(1,)''');
		parseAndFail('''OMG99a(,1,)''');
		parseAndFail('''OMG99a(1,)''');
		parseAndFail('''OMG99a((1)''');
		parseAndFail('''OMG99a((1))''');
		parseAndFail('''OMG99a(1))''');
	}
	
	@Test
	public def void testOneCitationWithManyPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2"))
			),
			'''OMG99a  ( 1, 2 )  '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3"))
			),
			'''OMG99a(1,2,3)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3"))
			),
			'''OMG99a  (1, 2, 3)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3"))
			),
			'''OMG99a    (   1   ,   2,3  )'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3"))
			),
			'''   OMG99a    ( 1,2,3    )    '''
		);
		
		parseAndFail('''OMG99a(,1,2,3)''');
		parseAndFail('''OMG99a(1,2,3,)''');
		parseAndFail('''OMG99a(,1,2,3,)''');
		parseAndFail('''OMG99a(1,2,,3,)''');
		parseAndFail('''OMG99a(1,2,3))''');
		parseAndFail('''OMG99a((1,2,3))''');
		parseAndFail('''OMG99a(1,2,3))''');
	}
	
	@Test
	public def void testTwoCitationsWithoutPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''OMG99a,Reiser12'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''     OMG99a,   Reiser12'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''OMG99a    , Reiser12   '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''   OMG99a,Reiser12   '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''   OMG99a (  ),Reiser12    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12")
			),
			'''   OMG99a (  ),Reiser12  ()  '''
		);
		
		parseAndFail('''OMG99a,Reiser12,''');
		parseAndFail(''',OMG99a,   Reiser12,''');
	}
	
	@Test
	public def void testTwoCitationsWithAndWithoutPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12")
			),
			'''OMG99a(1,2,3),Reiser12'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12")
			),
			'''OMG99a  (1, 2, 3), Reiser12   '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12")
			),
			'''OMG99a    (   1   ,   2,3  ), Reiser12 ()  '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1", "2", "3"))
			),
			'''   OMG99a   , Reiser12    ( 1,2,3    )    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1", "2", "3"))
			),
			'''   OMG99a(), Reiser12    ( 1,2,3    )    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1", "2", "3"))
			),
			'''   OMG99a (    )   , Reiser12    ( 1,2,3    )    '''
		);
		
		parseAndFail('''OMG99a(1,2,3),Reiser12,''');
		parseAndFail('''OMG99a(1,2,3)Reiser12''');
		parseAndFail('''OMG99a(1,2,3),,Reiser12()''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,,2)''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2''');
		parseAndFail('''OMG99a(1,2,3, Reiser12()''');
		parseAndFail('''OMG99a, Reiser12(,,)  ''');
	}
	
	@Test
	public def void testTwoCitationsWithPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''OMG99a(1,2,3),Reiser12(1,2)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''OMG99a  (1, 2, 3), Reiser12(1 , 2  )   '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''OMG99a    (   1   ,   2,3  ), Reiser12 (   1,2   )  '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''   OMG99a(1,2,3)   , Reiser12    ( 1    ,   2    )    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''   OMG99a(1,2,    3), Reiser12    ( 1  ,2    )    '''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2", "3")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''   OMG99a (  1   ,2   ,3  )   , Reiser12    ( 1,2    )    '''
		);
		
		parseAndFail('''OMG99a(1,2,3),Reiser12(1,2),''');
		parseAndFail('''OMG99a(1,2,3)Reiser12(1,2)''');
		parseAndFail('''OMG99a(1,2,3),,Reiser12(1,2)''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,,2)''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2''');
		parseAndFail('''OMG99a(1,2,3, Reiser12(1,2)''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2,)  ''');
	}
	
	@Test
	public def void testMultipleCitationsWithMixedPages() {
		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''OMG99a,Reiser12(1,2)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1")),
				new CitationReference("Reiser12", newLinkedList("1", "2"))
			),
			'''OMG99a  (1)   ,Reiser12(1,2)'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("1", "2")),
				new CitationReference("Reiser12"),
				new CitationReference("Mews16")
			),
			'''OMG99a  ( 1, 2 )   ,Reiser12(),  Mews16'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1", "2")),
				new CitationReference("Mews16")
			),
			'''OMG99a,Reiser12(1,2),  Mews16'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a"),
				new CitationReference("Reiser12", newLinkedList("1","2")),
				new CitationReference("Mews16", newLinkedList("1","2","3"))
			),
			'''OMG99a,Reiser12(1,2),  Mews16 ( 1, 2, 3 )'''
		);

		parseAndAssert(
			newLinkedList(
				new CitationReference("OMG99a", newLinkedList("2", "4", "3")),
				new CitationReference("Reiser12", newLinkedList("1","2")),
				new CitationReference("Mews16", newLinkedList("1","2","3"))
			),
			'''OMG99a(2,4,3)   ,Reiser12(1,2),  Mews16 ( 1, 2, 3 )'''
		);

		parseAndFail('''OMG99a(1,2,3),Reiser12(1,2),Mews16,''');
		parseAndFail('''OMG99a(1,2,3)Reiser12(1,2),Mews16''');
		parseAndFail('''OMG99a(1,2,3),,Reiser12(1,2),Mews16''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,,2),Mews16''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2 ,  Mews1''');
		parseAndFail('''OMG99a(1,2,3, Reiser12(1,2)  ,    Mews16''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2,) ,  Mews16  ''');
		parseAndFail('''OMG99a(1,2,3), Reiser12(1,2) ,  Mews16(,)  ''');
		parseAndFail('''OMG99a(), Reiser12() ,  Mews16)  ''');
		parseAndFail('''OMG99a(), Reiser12() ,  Mews16(  ''');
	}
}
