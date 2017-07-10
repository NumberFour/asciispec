require 'asciidoctor'
require_relative "../../main/resources/ext/callout.rb"
require "test/unit"

class TestCalloutInlineMacroProcessor < Test::Unit::TestCase
	
	def test_single
		input = "call:1[]"

	assert_equal("<div class=\"paragraph\">\n<p><b class=\"conum\">(1)</b></p>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

	def test_inside_after_table
		input = 
		"|===\n"\
		"| A single callout call:1[] inside a table\n"\
		"|===\n"
		"The first callout: call:1[]\n"\
		"And some duplicates: call:2[] call:2[] call:20[]"

	assert_equal("<table class=\"tableblock frame-all grid-all spread\">\n"\
		"<colgroup>\n<col style=\"width: 100%;\">\n</colgroup>\n<tbody>\n<tr>\n"\
		"<td class=\"tableblock halign-left valign-top\"><p class=\"tableblock\">"\
		"A single callout <b class=\"conum\">(1)</b> inside a table</p></td>\n"\
		"</tr>\n</tbody>\n</table>", 
		Asciidoctor::Document.new(input).render)
	end

	def test_source_block
		input = 
		"[source]\n"\
		"----\n"\
		"$ xz -z0 /dev/urandom -c > /dev/sd'XY' call:1[]\n"\
		"----"

	assert_equal("<div class=\"listingblock\">\n<div class=\"content\">\n"\
		"<pre class=\"highlight\"><code>$ xz -z0 /dev/urandom -c &gt; /dev/sd'XY'"\
		" call:1[]</code></pre>\n</div>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end
end