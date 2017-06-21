package eu.numberfour.asciispec.processors;

/**
 * Registration class for the {@link InlineTaskLinkProcessor} processor.
 *
 * If you rename or move this class, do not forget to update the
 * META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry file!
 */
public class InlineTaskLinkExtension extends ProcessorExtension {
	@Override
	protected void register(JavaExtensionRegistry registry) {
		registry.inlineMacro("task", InlineTaskLinkProcessor.class);
		registry.inlineMacroToSiblingBlock("sidebar", "task", InlineTaskLinkProcessor.class);
	}
}
