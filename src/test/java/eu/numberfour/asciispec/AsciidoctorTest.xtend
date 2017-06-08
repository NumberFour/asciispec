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
package eu.numberfour.asciispec

import eu.numberfour.asciispec.processors.ProcessorExtension
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintStream
import java.io.Reader
import java.io.StringWriter
import java.util.HashMap
import java.util.Objects
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.^extension.RubyExtensionRegistry
import org.junit.After
import org.junit.Before

import static org.asciidoctor.OptionsBuilder.*
import static org.junit.Assert.*
import eu.numberfour.asciispec.hacks.HackJRuby

/**
 *
 */
class AsciidoctorTest {
	protected static enum Backend {
		DOCBOOK,HTML5
	}

	private static def backendName(Backend backend) {
		switch (backend) {
			case DOCBOOK: "docbook"
			case HTML5: "html5"
		}
	}

	protected Asciidoctor doc;
	protected RubyExtensionRegistry rubyRegistry;

	@Before
	public def void createDoctor() {
		HackJRuby.disableSecureRandoms();
		doc = Asciidoctor.Factory.create();
		rubyRegistry = doc.rubyExtensionRegistry;
		
		/* By default the creation of an asciidoctor instance will register all extensions mentioned in
		 * src/main/resources/META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry
		 * However, for testing purposes we want to be able to selectively register extensions required for each test.
		 * That is why at this point we need to unregister all extensions which were registered automatically.
		 * The required extensions will later be registered manually by each individual test.
		 */
		ProcessorExtension.unregisterAllExtensions(doc);
	}

	def void registerRubyExtensionBlock(String fileName, String extensionClassName, String macroName) {
		val resStream = Class.getResourceAsStream(fileName)
		rubyRegistry.loadClass(resStream);
		rubyRegistry.block(macroName, extensionClassName);
	}

	def void registerRubyExtensionDocinfoProcessor(String fileName, String extensionClassName) {
		val resStream = Class.getResourceAsStream(fileName)
		rubyRegistry.loadClass(resStream);
		rubyRegistry.docinfoProcessor(extensionClassName);
	}

	@After
	public def void destroyDoctor() {
		doc.shutdown();
		doc = null;
	}

	/**
	 * In addition to {@link #convertAndAssert(CharSequence, String, String)}, this method asserts that the standard
	 * output produced during asciidoc processing <b>will be equal</b> to the substring <code>expectedConsoleOut</code>.
	 */
	protected def void convertFileAndAssertError(CharSequence expectedOutput, String baseDirStr, String fileName,
		CharSequence expectedConsoleOut) {

		internalConvertAndAssertError(expectedConsoleOut, true, [
			val String convertedInput = convertFile(baseDirStr, fileName, null);
			assertEquals(expectedOutput.toString, convertedInput);
		]);
	}

	/**
	 * In addition to {@link #convertAndAssert(CharSequence, String, String)}, this method asserts that the standard
	 * output produced during asciidoc processing will be equal to the substring <code>expectedConsoleOut</code>.
	 */
	protected def void convertFileAndAssertErrorContains(CharSequence expectedOutput, String baseDirStr, String fileName,
		CharSequence expectedConsoleOut) {

		convertFileAndAssertErrorContains(expectedOutput, baseDirStr, fileName, null, expectedConsoleOut);
	}

	/**
	 * In addition to {@link #convertFileAndAssertErrorContains(CharSequence, String, String,
		HashMap<String, Object>, CharSequence)}, this method accepts also arguments.
	 */
	protected def void convertFileAndAssertErrorContains(CharSequence expectedOutput, String baseDirStr, String fileName,
		HashMap<String, Object> options, CharSequence expectedConsoleOut) {

		internalConvertAndAssertError(expectedConsoleOut, false, [
			val String convertedInput = convertFile(baseDirStr, fileName, options);
			assertEquals(expectedOutput.toString, convertedInput);
		]);
	}

	/**
	 * This method converts the asciidoc source to html backend, then hijacks the console and verifies that the
	 * expected string is printed. This action consumes the console message.
	 */
	protected def void convertStringAndAssertErrorContains(CharSequence expectedOutput, String input,
		String expectedConsoleOutSubstring) {
		convertStringAndAssertErrorContains(expectedOutput,input,expectedConsoleOutSubstring, Backend.HTML5)
	}

	/**
	 * This method converts the asciidoc source to specified backend, then hijacks the console and verifies that
	 * the expected string is printed. This action consumes the console message.
	 */
	protected def void convertStringAndAssertErrorContains(CharSequence expectedOutput, String input,
		String expectedConsoleOutSubstring, Backend backend) {

		internalConvertAndAssertError(expectedConsoleOutSubstring, false, [
			val String convertedInput = convert(input,backend);
			assertEquals(expectedOutput.toString, convertedInput);
		]);
	}

	private def void internalConvertAndAssertError(CharSequence expectedOutput, boolean requireFullMatch,
		Runnable baseAssertions) {
		val PrintStream oldErr = System.err;

		val ByteArrayOutputStream baos = new ByteArrayOutputStream();
		val PrintStream newErr = new PrintStream(baos);
		System.setErr(newErr);

		val String actualOutput = try {
			baseAssertions.run();
			System.err.flush();
			baos.toString()
		} finally {
			System.setOut(oldErr);
		};

		if(requireFullMatch) {
			if(!Objects.equals(expectedOutput.toString, actualOutput)) {
				val msg = "mismatch of error output; expected:\n"
					+ expectedOutput + "\n"
					+ "actual:\n"
					+ actualOutput;
				println(msg); // print message to stdout for easier debugging
				fail(msg);
			}
		} else {
			if(!actualOutput.contains(expectedOutput.toString)) {
				val msg = "error output does not contain the expected substring; expected substring:\n"
					+ expectedOutput + "\n"
					+ "actual console output:\n"
					+ actualOutput;
				println(msg); // print message to stdout for easier debugging
				fail(msg);
			}
		}
	}

	protected def void convertAndAssert(CharSequence expectedOutput, String input) {
		val String actualOutput = convert(input);
		assertEquals(expectedOutput.toString, actualOutput);
	}

	protected def void convertAndAssert(CharSequence expectedOutput, String input, Backend backend) {
		val String actualOutput = convert(input, backend);
		assertEquals(expectedOutput.toString(), actualOutput);
	}

	protected def void convertFileAndAssert(CharSequence expectedOutput, String baseDirStr, String filename) throws IOException {
		convertFileAndAssert(expectedOutput, baseDirStr, filename, null);
	}

	protected def void convertFileAndAssert(CharSequence expectedOutput, String baseDirStr, String filename, HashMap<String, Object> options) throws IOException {
		val String actualOutput = convertFile(baseDirStr, filename, options);
		assertEquals(expectedOutput.toString, actualOutput);
	}

	protected def String convert(String input) {
		return convert(input, Backend.HTML5);
	}

	protected def String convert(String input, Backend backend) {
		val options = getOptions(null, new File("."), null, backend);
		return doc.convert(input, options);
	}

	protected def String convertFile(String baseDirStr, String filename, HashMap<String, Object> options) {
		try {
			val File baseDir = new File(baseDirStr);
			val File inputFile = new File(baseDir, filename);
			val Reader inputReader = new BufferedReader(new FileReader(inputFile))
			val StringWriter outputWriter = new StringWriter();

			val optionsExt = getOptions(options, baseDir, inputFile, Backend.HTML5);

			doc.convert(inputReader, outputWriter, optionsExt);
			return outputWriter.buffer.toString();
		} catch (IOException e) {
			throw new RuntimeException("IOException while converting asciidoc to output", e);
		}
	}

	protected def OptionsBuilder getOptions(HashMap<String, Object> options, File baseDir, File inputFile, Backend backend) {
		val attributes = if (options == null)
							new HashMap<String, Object>()
						else
							options;

		var inputFileName = "<DIRECT_INPUT>";
		if (inputFile !== null)
			inputFileName = inputFile.getAbsolutePath().toString();

		attributes.put("docfile", inputFileName);

		options()
			.safe(SafeMode.UNSAFE)
			.baseDir(baseDir)
			.attributes(attributes)
			.option("sourcemap", true)
			.option(Options.BACKEND, backendName(backend));
	}
}
