package eu.numberfour.asciispec.hacks;

import java.lang.reflect.Field;

public class HackJRuby {

	/**
	 * JRuby is enabling secure random numbers on default. This causes a delay
	 * of approx. 4 seconds at every start of JRuby and thus AsciidoctorJ and
	 * AsciiSpec. Moreover, secure random numbers seem unnecessary in the use
	 * case of Asciispec.
	 */
	static public boolean disableSecureRandoms() {
		try {
			Class<?> jRubySecHelper = Class.forName("org.jruby.util.SecurityHelper");
			Field attempted = jRubySecHelper.getDeclaredField("attempted");

			attempted.setAccessible(true);
			attempted.setBoolean(null, true);
			attempted.setAccessible(false);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
