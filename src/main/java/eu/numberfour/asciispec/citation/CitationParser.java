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
package eu.numberfour.asciispec.citation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.SimpleParser;

/**
 * A parser for citation references.
 *
 * <pre>
 * CITATION ::= "cite:[" REF { "," REF } "]"
 * REF      ::= CITEKEY [ "(" PAGE { "," PAGE } ")" ]
 * CITEKEY  ::= STRING
 * PAGE     ::= STRING
 * STRING   ::= CHAR { CHAR }
 * CHAR     ::= Any character except (),
 * </pre>
 */
public class CitationParser extends SimpleParser {
	private static enum TokenType {
		STRING, COMMA, OPARENTHESIS, CPARENTHESIS, EOF
	}

	private static class Tokenizer extends SimpleTokenizer<TokenType> {

		/**
		 * Creates a new tokenizer that processes the given string.
		 *
		 * @param data
		 *            the string to process
		 */
		public Tokenizer(String data) {
			super(data);
		}

		@Override
		protected Token<TokenType> readToken() throws ParseException {
			while (!eof()) {
				final TokenPosition position = getTokenPosition();
				final char c = getCurrentChar();
				switch (c) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					advance();
					break;
				case ',':
					advance();
					return createTokenFromSubstring(TokenType.COMMA, position);
				case '(':
					advance();
					return createTokenFromSubstring(TokenType.OPARENTHESIS, position);
				case ')':
					advance();
					return createTokenFromSubstring(TokenType.CPARENTHESIS, position);
				default:
					return readStringToken();
				}
			}

			return new Token<>(TokenType.EOF, getTokenPosition());
		}

		private Token<TokenType> readStringToken() {
			final TokenPosition position = getTokenPosition();
			readUntil(" \t\n\r(),");
			return createTokenFromSubstring(TokenType.STRING, position);
		}
	}

	/**
	 * Parses the given string containing citation references entries.
	 *
	 * @param data
	 *            the data to parse
	 * @return the citation references
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static List<CitationReference> parse(String data) throws ParseException {
		return new CitationParser(data).parse();
	}

	private final Tokenizer tokenizer;

	private CitationParser(String data) {
		this.tokenizer = new Tokenizer(Objects.requireNonNull(data));
	}

	private List<CitationReference> parse() throws ParseException {
		Token<TokenType> token = tokenizer.nextToken().expect(TokenType.STRING, TokenType.EOF);
		if (token.hasType(TokenType.EOF))
			return Collections.emptyList();

		List<CitationReference> result = new LinkedList<>();
		tokenizer.pushToken(token);
		do {
			result.add(parseReference());
		} while (tokenizer.nextToken().expect(TokenType.COMMA, TokenType.EOF).hasType(TokenType.COMMA));

		return result;
	}

	private CitationReference parseReference() throws ParseException {
		String citeKey = tokenizer.nextToken().expect(TokenType.STRING).data;
		List<String> pages = parsePages();
		return new CitationReference(citeKey, pages);
	}

	private List<String> parsePages() throws ParseException {
		Token<TokenType> token = tokenizer.nextToken().expect(TokenType.EOF, TokenType.OPARENTHESIS, TokenType.COMMA);
		if (!token.hasType(TokenType.OPARENTHESIS)) {
			tokenizer.pushToken(token);
			return Collections.emptyList();
		}

		token = tokenizer.nextToken().expect(TokenType.CPARENTHESIS, TokenType.STRING);
		if (token.hasType(TokenType.CPARENTHESIS))
			return Collections.emptyList();

		List<String> result = new LinkedList<>();
		tokenizer.pushToken(token);
		do {
			result.add(tokenizer.nextToken().expect(TokenType.STRING).data);
		} while (tokenizer.nextToken().expect(TokenType.CPARENTHESIS, TokenType.COMMA).hasType(TokenType.COMMA));

		return result;
	}
}
