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
package eu.numberfour.asciispec.processors;

import java.util.Map;
import java.util.WeakHashMap;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.spi.ExtensionRegistry;

/**
 *
 */
public abstract class ProcessorExtension implements ExtensionRegistry {

	private static Map<Asciidoctor, JavaExtensionRegistry> sharedExtensionRegistries = new WeakHashMap<>();

	@Override
	public void register(Asciidoctor asciidoctor) {
		register(getSharedRegistry(asciidoctor));
	}

	public static void unregisterAllExtensions(Asciidoctor asciidoctor) {
		asciidoctor.unregisterAllExtensions();
		// Better safe than sorry - we don't know if there won't be multiple instances of Asciidoctor running in several
		// threads.
		synchronized (sharedExtensionRegistries) {
			sharedExtensionRegistries.remove(asciidoctor);
		}
	}

	private JavaExtensionRegistry getSharedRegistry(Asciidoctor asciidoctor) {
		// Better safe than sorry - we don't know if there won't be multiple instances of Asciidoctor running in several
		// threads.
		synchronized (sharedExtensionRegistries) {
			JavaExtensionRegistry result = sharedExtensionRegistries.get(asciidoctor);
			if (result == null) {
				result = new JavaExtensionRegistry(asciidoctor.javaExtensionRegistry());
				sharedExtensionRegistries.put(asciidoctor, result);
			}
			return result;
		}
	}

	/**
	 * Perform the registration using the given extension registry.
	 *
	 * @param registry
	 *            the registry to use
	 */
	protected abstract void register(JavaExtensionRegistry registry);
}
