require 'asciidoctor'
require_relative "../../main/resources/ext/xmlreqblock.rb"
require "test/unit"

# Register the TODO block processor
Extensions.register do
	block XmlReqBlock
end

class TestReqBlock < Test::Unit::TestCase
	def test_basic
		input = ".Some title\n[req,id=R-1,version=1]\n--\nA Super Requirement\n--"

	assert_equal("<div id=\"R-1\" class=\"admonitionblock requirement\">\n<table>\n"\
		"<tr>\n<td class=\"icon\">\n<div class=\"title\">Requirement: </div>\n</td>\n"\
		"<td class=\"content\">\n<div class=\"title\">Some title</div>\n<div class=\""\
		"paragraph\">\n<p>\n<anchor xml:id=\"Req-R-1\" xreflabel=\"[Req-R-1]\"/>\n<emphasis"\
		" role=\"strong\">Requirement: R-1:</emphasis>\n<link linkend=\"Req-R-1\">Some "\
		"title</link> (ver. 1)</simpara>\n    <simpara>\n\nA Super Requirement</p>\n</div>\n"\
		"</td>\n</tr>\n</table>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

=begin
add a test to catch errors

	def test_no_title
		input = "\n\n[req,id=R-1,version=1]\n--\nA Super Requirement\n--"

	assert_raise(NoMethodError) do
		Asciidoctor::Document.new(input).render
	end
	end
=end

end