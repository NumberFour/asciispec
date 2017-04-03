package eu.numberfour.asciispec.processors;

/**
 * Registration class for the {@link ResolveReqIncludeProcessor} and
 * {@link ResolveReqInlinePreprocessor} processors.
 *
 * If you rename or move this class, do not forget to update the
 * META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry file!
 */
public class ResolveReqExtension extends ProcessorExtension {
	@Override
	protected void register(JavaExtensionRegistry registry) {
		registry.includeProcessor(ResolveReqIncludeProcessor.class);
		registry.preprocessor(ResolveReqInlinePreprocessor.class);
	}
}
