package eu.numberfour.asciispec.cli;

import org.asciidoctor.cli.AsciidoctorInvoker;

import eu.numberfour.asciispec.hacks.HackJRuby;

/**
 * Main class to invoke AsciidoctorJ with Asciispec.
 * <p>
 * This wrapper is necessary to execute Java code before AsciidoctorJ is
 * started. Note that AsciidoctorJ does not provide a callback for this.
 */
public class AsciiSpec {

	public static void main(String[] args) {
		HackJRuby.disableSecureRandoms();
		AsciidoctorInvoker.main(args);
	}

}
