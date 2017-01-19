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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.numberfour.asciispec.ParseException;
import eu.numberfour.asciispec.sourceindex.SourceIndexProcessor.LineInfo;

/**
 * Parses index files that where written by the {@link IndexFileWriter}. See {@link IndexFileWriter} for details of the
 * file's structure.
 */
public class IndexFileParser extends AbstractPQNParser {

	/**
	 * Creates a new parser that parses the file at the given location with the given encoding and using the given tag
	 * processor to process the tags.
	 *
	 * @param path
	 *            the path to the file to be parsed
	 * @param encoding
	 *            the encoding of the file to be parsed
	 * @throws IOException
	 *             if the file cannot be found or cannot be opened
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static SourceIndexDatabase parse(String path, Charset encoding)
			throws IOException, ParseException {
		return parse(Paths.get(path), encoding);
	}

	/**
	 * Creates a new parser that parses the file at the given location with the given encoding and using the given tag
	 * processor to process the tags.
	 *
	 * @param path
	 *            the path to the file to be parsed
	 * @param encoding
	 *            the encoding of the file to be parsed
	 * @throws IOException
	 *             if the file cannot be found or cannot be opened
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static SourceIndexDatabase parse(Path path, Charset encoding)
			throws IOException, ParseException {
		return parse(new String(Files.readAllBytes(path), encoding));
	}

	/**
	 * Parses the given string containing BibTex entries and using the given tag processor to process the tags.
	 *
	 * @param data
	 *            the data to parse
	 * @return the source index entries
	 * @throws ParseException
	 *             if an error occurs during parsing
	 */
	public static SourceIndexDatabase parse(String data) throws ParseException {
		return new IndexFileParser(data).parse();
	}

	private final Tokenizer tokenizer;

	private IndexFileParser(String data) {
		this.tokenizer = new Tokenizer(data);
	}

	/**
	 * <pre>
	stdlib_api#packages
		eu.numberfour.stdlib.model.common.api#src.n4js-gen
			n4.model
				DataObjectShortNames.adoc
					DataObjectShortNames::4::8::17
			n4.model.common
				AbstractTimeSpan.adoc
					AbstractTimeSpan::18::8::12
	 * </pre>
	 */
	private SourceIndexDatabase parse() throws ParseException {
		SourceIndexProcessor siProcessor = new SourceIndexProcessor();
		LineInfo lineInfo = new LineInfo();
		siProcessor.init();

		Token<TokenType> token;
		do {
			token = tokenizer.nextToken();
			lineInfo.tabCount = 0;
			while (token.type == TokenType.TAB) {
				token = tokenizer.nextToken().expect(TokenType.TAB, TokenType.NAME, TokenType.NEWLINE, TokenType.EOF);
				lineInfo.tabCount++;
			}

			if (token.type == TokenType.NAME) {
				String maybeElemAndPropData = token.data;
				lineInfo.partialLocation = unquote(maybeElemAndPropData);
				lineInfo.validNumbers = false;

				token = tokenizer.nextToken().expect(TokenType.NUMBER, TokenType.NEWLINE);
				if (token.type == TokenType.NUMBER) {
					String[] elemAndProp = unquoteElemAndProp(maybeElemAndPropData);
					lineInfo.partialLocation = elemAndProp[0];
					lineInfo.delimiter = elemAndProp[1];
					lineInfo.property = elemAndProp[2];

					lineInfo.validNumbers = true;
					lineInfo.lineNumber = Integer.parseInt(token.data);
					token = tokenizer.nextToken().expect(TokenType.NUMBER);
					lineInfo.offsetStart = Integer.parseInt(token.data);
					token = tokenizer.nextToken().expect(TokenType.NUMBER);
					lineInfo.offsetEnd = Integer.parseInt(token.data);

					token = tokenizer.nextToken().expect(TokenType.NAME, TokenType.NEWLINE, TokenType.EOF);
					lineInfo.hasTrueFolder = false;
					if (token.type == TokenType.NAME) {
						lineInfo.hasTrueFolder = true;
						lineInfo.trueRepository = token.data;
						token = tokenizer.nextToken().expect(TokenType.NAME);
						lineInfo.truePath = token.data;
						token = tokenizer.nextToken().expect(TokenType.NAME);
						lineInfo.trueProject = token.data;
						token = tokenizer.nextToken().expect(TokenType.NAME);
						lineInfo.trueFolder = token.data;
						token = tokenizer.nextToken().expect(TokenType.NEWLINE, TokenType.EOF);
					}
				}

				lineInfo.line = token.line;
				lineInfo.column = token.column;
				siProcessor.processLine(lineInfo);
			}

		} while (token.type == TokenType.NEWLINE);

		token.expect(TokenType.EOF);

		return siProcessor.getDatabase();
	}

}
