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
package eu.numberfour.asciispec;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A parser for Asciidoc arguments.
 */
public class AttributeParser extends SimpleParser {
	private static enum TokenType {
		STRING, KEY_VALUE_SEPARATOR, ENTRY_SEPARATOR, QUOTED_STRING, EOF
	}

	private static class Tokenizer extends SimpleTokenizer<TokenType> {
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
					advance();
					break;
				case '=':
					advance();
					return createTokenFromSubstring(TokenType.KEY_VALUE_SEPARATOR, position);
				case ',':
					advance();
					return createTokenFromSubstring(TokenType.ENTRY_SEPARATOR, position);
				case '"':
					advance();
					return readQuotedString();
				default:
					return readString();
				}
			}

			return new Token<>(TokenType.EOF, getTokenPosition());
		}

		private Token<TokenType> readQuotedString() throws ParseException {
			return readDelimitedStringToken('"', '\\', TokenType.QUOTED_STRING);
		}

		private Token<TokenType> readString() {
			TokenPosition startPosition = getTokenPosition();
			int endPosition = startPosition.offset;

			loop: while (!eof()) {
				final char c = getCurrentChar();
				switch (c) {
				case '=':
				case ',':
				case '"':
					break loop;
				case ' ':
				case '\t':
				case '\n':
					advance();
					break;
				default:
					advance();
					endPosition = getCurrentPosition();
					break;
				}
			}

			return createTokenFromSubstring(TokenType.STRING, startPosition, endPosition);
		}
	}

	/**
	 * Parses the given string containing key-value pairs or anonymous values and returns them as a map. Anonymous
	 * values are stored with integer indices starting at zero.
	 *
	 * @param data
	 *            the string to parse
	 * @return the parsed attributes
	 * @throws ParseException
	 *             if the string is not properly formatted
	 */
	public static Map<String, Object> parse(String data) throws ParseException {
		return new AttributeParser(data).parse();
	}

	private final Tokenizer tokenizer;
	private int argumentIndex;

	private AttributeParser(String data) {
		tokenizer = new Tokenizer(data);
		argumentIndex = 0;
	}

	private Map<String, Object> parse() throws ParseException {
		Map<String, Object> result = new HashMap<>();

		Token<TokenType> token = tokenizer.nextToken();
		if (token.type != TokenType.EOF) {
			tokenizer.pushToken(token);

			do {
				Entry<String, Object> argument = parseAttribute();
				result.put(argument.getKey(), argument.getValue());

				token = tokenizer.nextToken();
				token.expect(TokenType.ENTRY_SEPARATOR, TokenType.EOF);
			} while (token.type == TokenType.ENTRY_SEPARATOR);
		}

		return result;
	}

	private Entry<String, Object> parseAttribute() throws ParseException {
		Token<TokenType> token = tokenizer.nextToken();
		token.expect(TokenType.QUOTED_STRING, TokenType.STRING);

		String key = token.data;
		if (token.type == TokenType.QUOTED_STRING)
			return makeEntry(argumentIndex++, key);

		token = tokenizer.nextToken();
		token.expect(TokenType.ENTRY_SEPARATOR, TokenType.KEY_VALUE_SEPARATOR, TokenType.EOF);

		if (token.type != TokenType.KEY_VALUE_SEPARATOR) {
			tokenizer.pushToken(token);
			return makeEntry(argumentIndex++, key);
		}

		token = tokenizer.nextToken();
		token.expect(TokenType.STRING, TokenType.QUOTED_STRING);

		String value = token.data;
		return makeEntry(key, value);
	}

	private Entry<String, Object> makeEntry(Object key, Object value) {
		return new AbstractMap.SimpleEntry<>(key.toString(), value);
	}
}
