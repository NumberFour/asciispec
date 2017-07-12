require 'asciidoctor'
require_relative "../../main/resources/ext/xmldefblock.rb"
require "test/unit"

# Register the Def block processor
Extensions.register do
	block XmlDefBlock
end

class TestDefBlock < Test::Unit::TestCase

	def test_simple_xml
		input = ".The Title Of MY Definition\n[def]\n--\nMy Amazing Definition\n--"
		doc = Asciidoctor::Document.new ("#{input}"), :backend => 'docbook'
	
	assert_equal("<definition>\n<title>The Title Of MY Definition</title>\n<simpara>"\
		"\n<anchor xml:id=\"the_title_of_my_definition\" xreflabel=\"[the_title_of_my_definition]\""\
		"/>\n<emphasis role=\"strong\">Definition:</emphasis>\n<link linkend=\""\
		"the_title_of_my_definition\">The Title Of MY Definition</link></simpara>\n<simpara>\n\n"\
		"My Amazing Definition</simpara>\n</definition>", 
		doc.render)
	end

	def test_character_replacement_in_title_xml
		input = ".The so called \"title\"\n[def]\n--\nThe Definitive Test\n--"
		doc = Asciidoctor::Document.new ("#{input}"), :backend => 'docbook'

	assert_equal("<definition>\n<title>The so called \"title\"</title>\n<simpara>\n<anchor xml:id=\"the_so_called_&quot;title&quot;\" xreflabel=\"[the_so_called_&quot;title&quot;]\"/>\n<emphasis role=\"strong\">Definition:</emphasis>\n<link linkend=\"the_so_called_&quot;title&quot;\">The so called &quot;title&quot;</link></simpara>\n<simpara>\n\nThe Definitive Test</simpara>\n</definition>", 
		doc.render)
	end

	
	def test_simple_html
		input = ".The Title Of MY Definition\n[def]\n--\nMy Amazing Definition\n--"

	assert_equal("<div class=\"admonitionblock definition\">\n<table>\n<tr>\n<td "\
		"class=\"icon\">\n<div class=\"title\">Definition: </div>\n</td>\n<td class"\
		"=\"content\">\n<div class=\"title\">The Title Of MY Definition</div>\n<div"\
		" class=\"paragraph\">\n<p>\n<anchor xml:id=\"the_title_of_my_definition\" "\
		"xreflabel=\"[the_title_of_my_definition]\"/>\n<emphasis role=\"strong\">"\
		"Definition:</emphasis>\n<link linkend=\"the_title_of_my_definition\">The "\
		"Title Of MY Definition</link></simpara>\n<simpara>\n\nMy Amazing Definition"\
		"</p>\n</div>\n</td>\n</tr>\n</table>\n</div>", 
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