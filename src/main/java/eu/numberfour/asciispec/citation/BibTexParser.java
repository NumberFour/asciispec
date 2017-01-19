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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.SimpleParser;

/**
 *
 */
public class BibTexParser extends SimpleParser {
	private static enum TokenType {
		/**
		 * A BibTex Entry, value is the string after the leading '@', the string is terminated by whitespace or '{'.
		 */
		ENTRY,
		/**
		 * '{' character, used either as an entry delimiter or a string delimiter.
		 */
		OBRACE,
		/**
		 * '}' character, used either as an entry delimiter or a string delimiter.
		 */
		CBRACE,
		/**
		 * A tag name is a string that is terminated by whitespace or an equals sign.
		 */
		TAG_VALUE_SEPARATOR,
		/**
		 * The concatenation operator used with quoted strings, i.e. "This is" # "a string". The token data does not
		 * contain the quotation marks.
		 */
		CONCAT,
		/**
		 * A string that is terminated by ',', '}', '=', '#', or whitespace. The tokenizer will read the string until,
		 * but not including, the terminating character, which will be returned as the next token or skipped in the case
		 * of whitespace.
		 */
		STRING,
		/**
		 * A tag value enclosed in double quotation marks.
		 */
		TAG_VALUE_STRING,
		/**
		 * A tag value enclosed in curly braces containing LaTeX.
		 */
		TAG_VALUE_LATEX_STRING,
		/**
		 * A tag value that is a number (a sequence of digits).
		 */
		TAG_VALUE_NUMBER,
		/**
		 * A tag value that is a sequence of characters and that is not enclosed in either double quotation marks or
		 * curly braces and that is also not a number. Cannot contain any whitespace. These tag values are usually
		 * replaced during the parsing process.
		 */
		TAG_VALUE_REPLACEABLE_STRING,
		/**
		 * Separator between consecutive tags of an entry, i.e. ','.
		 */
		TAG_SEPARATOR,
		/**
		 * End of file.
		 */
		EOF
	}

	private static class Tokenizer extends SimpleTokenizer<TokenType> {
		public Tokenizer(String data) {
			super(data);
		}

		public Token<TokenType> readTagValueToken() throws ParseException {
			if (hasPushedTokens())
				throw new ParseException(getCurrentLine(), getCurrentColumn(),
						"Cannot read tag value with nonempty token stack");

			throwIfEof();
			readWhile(" \t\n\r");
			throwIfEof();

			final char c = getCurrentChar();
			switch (c) {
			case '"':
				advance();
				return readQuotedTagValueToken();
			case '{':
				advance();
				return readLatexTagValueToken();
			default:
				return readNumberOrReplaceableStringTagValueToken();
			}
		}

		private Token<TokenType> readQuotedTagValueToken() throws ParseException {
			return readDelimitedStringToken('"', '\\', TokenType.TAG_VALUE_STRING);
		}

		private Token<TokenType> readLatexTagValueToken() throws ParseException {
			final int openingBraceCount = readWhile("{") + 1; // one brace was already consumed
			final TokenPosition position = getTokenPosition();
			final String data = readLatexString(openingBraceCount);
			return new Token<>(TokenType.TAG_VALUE_LATEX_STRING, position, data.length(), data);
		}

		private Token<TokenType> readNumberOrReplaceableStringTagValueToken() {
			final TokenPosition position = getTokenPosition();

			boolean isNumber = true;
			loop: while (!eof()) {
				final char c = getCurrentChar();
				switch (c) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case ',':
				case '}':
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
					advance();
					break;
				default:
					advance();
					isNumber = false;
					break;
				}
			}

			if (isNumber)
				return createTokenFromSubstring(TokenType.TAG_VALUE_NUMBER, position);
			else
				return createTokenFromSubstring(TokenType.TAG_VALUE_REPLACEABLE_STRING, position);
		}

		@Override
		protected Token<TokenType> readToken() {
			while (!eof()) {
				final char c = getCurrentChar();
				final TokenPosition position = getTokenPosition();

				switch (c) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					advance();
					break;
				case '%':
					advance();
					readUntil("\n\r");
					break;
				case '@':
					advance();
					return readEntry();
				case '{':
					advance();
					return createTokenFromSubstring(TokenType.OBRACE, position);
				case '}':
					advance();
					return createTokenFromSubstring(TokenType.CBRACE, position);
				case '=':
					advance();
					return createTokenFromSubstring(TokenType.TAG_VALUE_SEPARATOR, position);
				case ',':
					advance();
					return createTokenFromSubstring(TokenType.TAG_SEPARATOR, position);
				case '#':
					advance();
					return createTokenFromSubstring(TokenType.CONCAT, position);
				default:
					return readString();
				}
			}

			return new Token<>(TokenType.EOF, getTokenPosition());
		}

		private Token<TokenType> readEntry() {
			// we have already consumed the '@' in readToken
			final TokenPosition position = getTokenPosition();
			readUntil("{ \t\n\r");
			return createTokenFromSubstring(TokenType.ENTRY, position);
		}

		private String readLatexString(final int openingBraceCount) throws ParseException {
			StringBuilder buffer = new StringBuilder();

			int braceDepth = openingBraceCount;

			boolean escaped = false;
			while (!eof() && braceDepth > 0) {
				final char c = getCurrentCharAndAdvance();

				switch (c) {
				case '\\':
					escaped = !escaped;
					buffer.append(c);
					break;
				case '{':
					if (!escaped)
						braceDepth++;
					buffer.append(c);
					escaped = false;
					break;
				case '}':
					if (!escaped)
						braceDepth--;
					if (braceDepth >= openingBraceCount)
						buffer.append(c);
					escaped = false;
					break;
				default:
					buffer.append(c);
					escaped = false;
					break;
				}
			}

			if (eof() && braceDepth > 0)
				throw new ParseException(getCurrentLine(), getCurrentColumn(),
						"Unexpected end of file while reading LaTeX string");

			return buffer.toString();
		}

		private Token<TokenType> readString() {
			final TokenPosition position = getTokenPosition();
			readUntil(",=}# \t\n\r");
			return createTokenFromSubstring(TokenType.STRING, position);
		}
	}

	/**
	 * Creates a new parser that parses the file at the given location with the given encoding and using the given tag
	 * processor to process the tags.
	 *
	 * @param path
	 *            the path to the file to be parsed
	 * @param encoding
	 *            the encoding of the file to be parsed
	 * @param tagProcessor
	 *            the processor for the tag values
	 *
	 * @throws IOException
	 *             if the file cannot be found or cannot be opened
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static List<BibliographyEntry> parse(String path, Charset encoding, BibTexTagProcessor tagProcessor)
			throws IOException, ParseException {
		return parse(Paths.get(path), encoding, tagProcessor);
	}

	/**
	 * Creates a new parser that parses the file at the given location with the given encoding and using the given tag
	 * processor to process the tags.
	 *
	 * @param path
	 *            the path to the file to be parsed
	 * @param encoding
	 *            the encoding of the file to be parsed
	 * @param tagProcessor
	 *            the processor for the tag values
	 *
	 * @throws IOException
	 *             if the file cannot be found or cannot be opened
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static List<BibliographyEntry> parse(Path path, Charset encoding, BibTexTagProcessor tagProcessor)
			throws IOException, ParseException {
		return parse(new String(Files.readAllBytes(path), encoding), tagProcessor);
	}

	/**
	 * Parses the given string containing BibTex entries and using the given tag processor to process the tags.
	 *
	 * @param data
	 *            the data to parse
	 * @param tagProcessor
	 *            the processor for the tag values
	 * @return the BibTeX entries
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static List<BibliographyEntry> parse(String data, BibTexTagProcessor tagProcessor) throws ParseException {
		return new BibTexParser(data).parse(tagProcessor);
	}

	private final Tokenizer tokenizer;

	private BibTexParser(String data) {
		this.tokenizer = new Tokenizer(data);
	}

	private List<BibliographyEntry> parse(BibTexTagProcessor tagProcessor) throws ParseException {
		List<BibliographyEntry> result = new LinkedList<>();

		Token<TokenType> token = tokenizer.nextToken();
		while (token.type == TokenType.ENTRY) {
			tokenizer.pushToken(token);
			result.add(parseEntry(tagProcessor));
			token = tokenizer.nextToken().expect(TokenType.ENTRY, TokenType.EOF);
		}

		token.expect(TokenType.EOF);

		return result;
	}

	private BibliographyEntry parseEntry(BibTexTagProcessor tagProcessor) throws ParseException {
		String entryType = tokenizer.nextToken().expect(TokenType.ENTRY).data;
		tokenizer.nextToken().expect(TokenType.OBRACE);
		String citeKey = tokenizer.nextToken().expect(TokenType.STRING).data;

		BibliographyEntry result = new BibliographyEntry(entryType, citeKey);
		parseTags(tagProcessor, result);

		return result;

	}

	private void parseTags(BibTexTagProcessor tagProcessor, BibliographyEntry entry) throws ParseException {
		while (tokenizer.nextToken().expect(TokenType.TAG_SEPARATOR,
				TokenType.CBRACE).type == TokenType.TAG_SEPARATOR) {
			String tagName = tokenizer.nextToken().expect(TokenType.STRING).data;
			tokenizer.nextToken().expect(TokenType.TAG_VALUE_SEPARATOR);
			String tagValue = readTagValue(tagName, tagProcessor);

			entry.addTag(tagName, tagValue);
		}
	}

	private String readTagValue(String tagName, BibTexTagProcessor tagProcessor) throws ParseException {
		final Token<TokenType> token = tokenizer.readTagValueToken();
		switch (token.type) {
		case TAG_VALUE_LATEX_STRING:
			return tagProcessor.processLatexTag(tagName, token.data);
		case TAG_VALUE_REPLACEABLE_STRING:
			return tagProcessor.processReplacementTag(tagName, token.data);
		default:
			return token.data;
		}
	}
}
