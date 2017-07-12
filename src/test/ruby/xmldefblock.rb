require 'asciidoctor'
require_relative "../../main/resources/ext/xmldefblock.rb"
require "test/unit"

# Register the Def block processor
Extensions.register do
	block XmlDefBlock
end

class TestDefBlock < Test::Unit::TestCase
	def test_simple
		input = ".The Title Of MY Definition\n[def]\n--\nMy Amazing Definition\n--"

	assert_equal("<div class=\"admonitionblock definition\">\n<table>\n<tr>\n<td "\
		"class=\"icon\">\n<div class=\"title\">Definition: </div>\n</td>\n<td class"\
		"=\"content\">\n<div class=\"title\">The Title Of MY Definition</div>\n<div"\
		" class=\"paragraph\">\n<p>\n<anchor xml:id=\"the_title_of_my_definition\" "\
		"xreflabel=\"[the_title_of_my_definition]\"/>\n<emphasis role=\"strong\">"\
		"Definition:</emphasis>\n<link linkend=\"the_title_of_my_definition\">The "\
		"Title Of MY Definition</link>\n\nMy Amazing Definition</p>\n</div>\n</td>\n"\
		"</tr>\n</table>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

=begin
add a test to catch errors

	def test_without_title
		input = "\n\n[def]\n--\nMy Amazing Definition\n--"

	assert_raise(NoMethodError) do
	end
	end
=end

end