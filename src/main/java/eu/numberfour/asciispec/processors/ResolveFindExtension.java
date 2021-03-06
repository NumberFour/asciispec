package eu.numberfour.asciispec.processors;

/**
 * Registration class for the {@link ResolveFindIncludeProcessor} and {@link ResolveFindInlinePreprocessor} processors.
 *
 * If you rename or move this class, do not forget to update the
 * META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry file!
 */
public class ResolveFindExtension extends ProcessorExtension {
	@Override
	protected void register(JavaExtensionRegistry registry) {
		registry.includeProcessor(ResolveFindIncludeProcessor.class);
		registry.preprocessor(ResolveFindInlinePreprocessor.class);
	}
}
