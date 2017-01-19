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

import java.util.LinkedList;
import java.util.List;

import eu.numberfour.asciispec.ParseException;

/**
 * Parses PQNs in srclnk macros and tests.
 */
public class PQNParser extends AbstractPQNParser {

	/**
	 * Parses the given string containing a PQN.
	 *
	 * @param pqn
	 *            the PQN to parse
	 * @return the source index entries
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static List<String> parse(String pqn) throws ParseException {
		return new PQNParser(pqn).parse();
	}

	private final Tokenizer tokenizer;

	private PQNParser(String data) {
		this.tokenizer = new Tokenizer(data);
	}

	/**
	 * A typical PQN looks like this:
	 *
	 * <pre>
	 * stdlib_api:packages:eu.numberfour.stdlib.format.api:src/n4js/n4/format/StructuredText:StructuredText#asString
	 * </pre>
	 */
	private List<String> parse() throws ParseException {
		LinkedList<String> pqnStack = new LinkedList<>();

		Token<TokenType> token;
		token = tokenizer.nextToken();
		while (token.hasType(TokenType.TAB, TokenType.NEWLINE)) {
			token = tokenizer.nextToken().expect(TokenType.TAB, TokenType.NAME, TokenType.NEWLINE, TokenType.EOF);
		}

		while (token.hasType(TokenType.NAME)) {
			pqnStack.add(0, token.data);
			token = tokenizer.nextToken().expect(TokenType.TAB, TokenType.NAME, TokenType.NEWLINE, TokenType.EOF);
		}

		String elemAndProp = pqnStack.remove(0);
		String[] elemAndPropArray = unquoteElemAndProp(elemAndProp);
		String element = elemAndPropArray[0];
		String delimiter = elemAndPropArray[1];
		String property = elemAndPropArray[2];
		if (!element.isEmpty())
			pqnStack.add(0, element);
		if (!delimiter.isEmpty())
			pqnStack.add(0, delimiter);
		if (!property.isEmpty())
			pqnStack.add(0, property);

		if (token.type != TokenType.EOF)
			token.expect(TokenType.EOF);

		return pqnStack;
	}

}
