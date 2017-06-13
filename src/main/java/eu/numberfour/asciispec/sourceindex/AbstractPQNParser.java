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

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.SimpleParser;

/**
 * This class is a common base for parser classes of these PQNs. PQNs are used at the following places:
 * <ul>
 * <li>in the index.idx file</li>
 * <li>in srclnk macros</li>
 * <li>in tests</li>
 * </ul>
 */
abstract class AbstractPQNParser extends SimpleParser {
	static enum TokenType {
		/**
		 * A hash character.
		 */
		COMMENT_LINE,
		/**
		 * A tabulator character.
		 */
		TAB,
		/**
		 * A newline character.
		 */
		NEWLINE,
		/**
		 * A tag name is a string that is terminated by whitespace or an equals sign.
		 */
		NAME,
		/**
		 * The label is used in srclinks.
		 */
		LABEL,
		/**
		 * A tag value enclosed in double quotation marks.
		 */
		NUMBER,
		/**
		 * End of file.
		 */
		EOF
	}

	static class Tokenizer extends SimpleTokenizer<TokenType> {
		public Tokenizer(String data) {
			super(data);
		}

		@Override
		protected Token<TokenType> readToken() throws ParseException {
			while (!eof()) {
				final char c = getCurrentChar();
				char cc;
				final TokenPosition position = getTokenPosition();

				switch (c) {
				case ' ':
				case '\r':
					advance();
					break;
				case '\t':
					advance();
					return createTokenFromSubstring(TokenType.TAB, position);
				case '\n':
					advance();
					cc = getCurrentChar();
					if (cc == '#') {
						readUntil("\n");
					}
					return createTokenFromSubstring(TokenType.NEWLINE, position);
				case '/':
				case ':':
					advance();
					cc = getCurrentChar();
					if (cc == ':') {
						advance();
						cc = getCurrentChar();
					}
					if ("1234567890".contains(String.valueOf(cc)))
						return readNumber();

					return readName();
				default:
					return readName();
				}
			}

			return new Token<>(TokenType.EOF, getTokenPosition());
		}

		protected Token<TokenType> readNumber() throws ParseException {
			final TokenPosition position = getTokenPosition();
			boolean isNumber = true;

			loop: while (!eof()) {

				final char c = getCurrentChar();
				switch (c) {
				case ':':
				case '\n':
					break loop;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '\r':
					advance();
					break;
				default:
					advance();
					isNumber = false;
					break;
				}
			}

			if (isNumber) {
				return createTokenFromSubstring(TokenType.NUMBER, position);
			}

			throw new ParseException(getCurrentLine(), getCurrentColumn(), "Number expected");
		}

		protected Token<TokenType> readName() throws ParseException {
			return readName("");
		}

		protected Token<TokenType> readName(String additionalStopChars) throws ParseException {
			final String stopChars = ":/\n\r\"" + additionalStopChars;
			final TokenPosition position = getTokenPosition();
			if (getCurrentChar() == '\"') {
				advance();
				readDelimitedString('\"', '\\');
			}

			do {
				readUntil(stopChars);
				if (getCurrentChar() == '\"') {
					advance();
					readDelimitedString('\"', '\\');
					continue;
				}
				break;
			} while (true);
			return createTokenFromSubstring(TokenType.NAME, position);
		}

	}

	String unquote(String data) {
		StringBuilder strb = new StringBuilder();
		for (int i = 0; i < data.length(); i++) {
			char cc = data.charAt(i);
			char pc = data.charAt(Math.max(0, i - 1));

			if (cc != '\"' || pc == '\\')
				strb.append(cc);
		}

		return strb.toString();
	}

	/**
	 * In the PQN, both, element and property names can consist of special characters. In case special characters occur,
	 * quotes have to be used. When parsing name containing special characters, the quotes are removed.
	 */
	static String[] unquoteElemAndProp(String data) {
		StringBuilder element = new StringBuilder();
		StringBuilder delimiter = new StringBuilder();
		StringBuilder property = new StringBuilder();
		StringBuilder strb = element;

		boolean inQuotes = false;
		for (int i = 0; i < data.length(); i++) {
			char cc = data.charAt(i);
			char pc = data.charAt(Math.max(0, i - 1));

			boolean quoteChar = cc == '\"' && pc != '\\';
			if (quoteChar)
				inQuotes = !inQuotes;

			if (!inQuotes) {
				if (cc == '#' || cc == '@') {
					int maxC = data.length();
					delimiter.append(cc);

					int nc = data.charAt(Math.min(maxC, i + 1));
					if (nc == '<' || nc == '>') {
						delimiter.append(nc);
						i++;
					}

					strb = property;
					continue;
				}
			}

			if (!quoteChar)
				strb.append(cc);
		}

		String[] elemAndProp = new String[3];
		elemAndProp[0] = element.toString();
		elemAndProp[1] = delimiter.toString();
		elemAndProp[2] = property.toString();
		return elemAndProp;
	}

}
