require 'asciidoctor'
require_relative "../../main/resources/ext/xmlreqblock.rb"
require "test/unit"

# Register the Req block processor
Extensions.register do
	block XmlReqBlock
end

class TestReqBlock < Test::Unit::TestCase
	def test_basic_xml
		input = ".Some title\n[req,id=R-1,version=1]\n--\nA Super Requirement\n--"
		doc = Asciidoctor::Document.new ("#{input}"), :backend => 'docbook'

	assert_equal("<requirement xml:id=\"R-1\">\n<title>Some title</title>\n<simpara>"\
		"\n<anchor xml:id=\"Req-R-1\" xreflabel=\"[Req-R-1]\"/>\n<emphasis role=\"strong"\
		"\">Requirement: R-1:</emphasis>\n<link linkend=\"Req-R-1\">Some title</link> (ver. 1)"\
		"</simpara>\n    <simpara>\n\nA Super Requirement</simpara>\n</requirement>", 
		doc.render)
	end

	def test_character_replacement_in_title_xml
		input = ".The so called \"title\"\n[req,id=R-1,version=1]\n--\nThe most superlative Requirement\n--"
		doc = Asciidoctor::Document.new ("#{input}"), :backend => 'docbook'

	assert_equal("<requirement xml:id=\"R-1\">\n<title>The so called \"title\"</title>"\
		"\n<simpara>\n<anchor xml:id=\"Req-R-1\" xreflabel=\"[Req-R-1]\"/>\n<emphasis "\
		"role=\"strong\">Requirement: R-1:</emphasis>\n<link linkend=\"Req-R-1\">The so"\
		" called \"title\"</link> (ver. 1)</simpara>\n    <simpara>\n\nThe most superlative"\
		" Requirement</simpara>\n</requirement>", 
		doc.render)
	end

	def test_multiple_replacements_in_title_xml
		input = ".That's like, your \"opinion\" & stuff\n[req,id=R-1,version=1]\n--\nThe dude abides\n--"
		doc = Asciidoctor::Document.new ("#{input}"), :backend => 'docbook'

	assert_equal("<requirement xml:id=\"R-1\">\n<title>That&#8217;s like, your \"opinion\""\
		" &amp; stuff</title>\n<simpara>\n<anchor xml:id=\"Req-R-1\" xreflabel=\"[Req-R-1]\""\
		"/>\n<emphasis role=\"strong\">Requirement: R-1:</emphasis>\n<link linkend=\"Req-R-1\""\
		">That's like, your \"opinion\" &amp; stuff</link> (ver. 1)</simpara>\n    <simpara>\n"\
		"\nThe dude abides</simpara>\n</requirement>", 
		doc.render)
	end

	def test_basic_html
		input = ".Some title\n[req,id=R-1,version=1]\n--\nA Super Requirement\n--"
		doc = Asciidoctor::Document.new ("#{input}")

	assert_equal("<div id=\"R-1\" class=\"admonitionblock requirement\">\n<table>\n"\
		"<tr>\n<td class=\"icon\">\n<div class=\"title\">Requirement: </div>\n</td>\n"\
		"<td class=\"content\">\n<div class=\"title\">Some title</div>\n<div class=\""\
		"paragraph\">\n<p>\n<anchor xml:id=\"Req-R-1\" xreflabel=\"[Req-R-1]\"/>\n<emphasis"\
		" role=\"strong\">Requirement: R-1:</emphasis>\n<link linkend=\"Req-R-1\">Some "\
		"title</link> (ver. 1)</simpara>\n    <simpara>\n\nA Super Requirement</p>\n</div>"\
		"\n</td>\n</tr>\n</table>\n</div>", 
		doc.render)
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
