require 'asciidoctor'
require_relative "../../main/resources/ext/todo.rb"
require "test/unit"

# Register the TODO block processor
Extensions.register do
	block TodoBlock
	docinfo_processor TodoBlockDocinfo
end

class TestTodoBlock < Test::Unit::TestCase
	def test_simple
		input = "[TODO]\nmy reminder"

	assert_equal("<div class=\"admonitionblock todo\">\n<table>\n<tr>\n<td "\
		"class=\"icon\">\n<div class=\"title\">Todo</div>\n</td>\n<td class=\""\
		"content\">\n<div class=\"paragraph\">\n<p>my reminder</p>\n</div>\n</td>"\
		"\n</tr>\n</table>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

	def test_with_delimiter
		input = 
		"[TODO]\n====\nmy reminder\n===="

	assert_equal("<div class=\"admonitionblock todo\">\n<table>\n<tr>\n<td class="\
		"\"icon\">\n<div class=\"title\">Todo</div>\n</td>\n<td class=\"content\">"\
		"\n<div class=\"paragraph\">\n<p>my reminder</p>\n</div>\n</td>\n</tr>\n</table>"\
		"\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

	def test_simple_with_title
		input = 
		".My Amazing Title\n[TODO]\nDON'T 4GET"

	assert_equal("<div class=\"admonitionblock todo\">\n<table>\n<tr>\n<td class=\""\
		"icon\">\n<div class=\"title\">Todo</div>\n</td>\n<td class=\"content\">\n"\
		"<div class=\"title\">My Amazing Title</div>\n<div class=\"paragraph\">\n<p>"\
		"DON&#8217;T 4GET</p>\n</div>\n</td>\n</tr>\n</table>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end

	def test_delimited_with_title
		input = 
		".Entitled\n[TODO]\n====\npush it to delimit\n===="

	assert_equal("<div class=\"admonitionblock todo\">\n<table>\n<tr>\n<td class=\"icon"\
		"\">\n<div class=\"title\">Todo</div>\n</td>\n<td class=\"content\">\n<div class="\
		"\"title\">Entitled</div>\n<div class=\"paragraph\">\n<p>push it to delimit</p>\n"\
		"</div>\n</td>\n</tr>\n</table>\n</div>", 
		Asciidoctor::Document.new(input).render)
	end
end