package eu.numberfour.asciispec.hacks;

import java.lang.reflect.Field;

import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Processor;

public class HackProcessors {

	/**
	 * This hack is to reset the field {@link Processor#configFinalized} to
	 * {@false}.
	 * <p>
	 * This is necessary when a processor is reused, since the method
	 * {@link Processor#setConfig(java.util.Map)} will be called then. However,
	 * this is not true for {@link IncludeProcessor} and {@link Preprocessor}
	 * because the {@link IncludeProcessor} won't call {@code setConfig} and the
	 * {@link Preprocessor} is always new instantiated.
	 * <p>
	 * The reuse of processors is necessary to convert multiple files during a
	 * single call of Asciidoctor. For example, the following call will
	 * recursively convert all adoc files in the working directory:
	 * <code>asciispec "**.*"</code>
	 */
	static public void resetConfigFinalized(Processor processor) {
		try {
			Field configFinalized = Processor.class.getDeclaredField("configFinalized");
			configFinalized.setAccessible(true);
			configFinalized.set(processor, false);
		} catch (Exception e) {
			throw new RuntimeException("Could not reset the AsciiDoctorJ Processor", e);
		}
	}

}
