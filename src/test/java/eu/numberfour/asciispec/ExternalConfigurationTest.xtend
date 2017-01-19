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

import java.io.IOException
import org.junit.Test

/**
 *
 */
class ExternalConfigurationTest extends AsciidoctorTest {
	@Test
	public def void test() throws IOException {
		// If the include guard works properly, then the configuration value in every included file and in the master
		// file must always be the one defined in Config.adoc - it must not be overwritten by the value in Config_Clone.adoc.
		// Note that for this to work, both files must share the same include guard name.

		convertFileAndAssert('''
		<div class="paragraph">
		<p>Value before subsections in master: original_value</p>
		</div>
		<div class="paragraph">
		<p>Value in subsection 1: original_value</p>
		</div>
		<div class="paragraph">
		<p>Value between subsections in master: original_value</p>
		</div>
		<div class="paragraph">
		<p>Value in subsection 2: original_value</p>
		</div>
		<div class="paragraph">
		<p>Value after subsections in master: original_value</p>
		</div>''',
		"src/test/resources/data/external_configuration", "Master.adoc")
	}
}
