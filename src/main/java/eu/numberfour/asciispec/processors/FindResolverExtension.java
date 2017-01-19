package eu.numberfour.asciispec.processors;

/**
 * Registration class for the {@link FindResolveIncludeProcessor} and {@link FindResolverInlinePreprocessor} processors.
 *
 * If you rename or move this class, do not forget to update the
 * META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry file!
 */
public class FindResolverExtension extends ProcessorExtension {
	@Override
	protected void register(JavaExtensionRegistry registry) {
		registry.includeProcessor(FindResolveIncludeProcessor.class);
		registry.preprocessor(FindResolverInlinePreprocessor.class);
	}
}
