require 'asciidoctor/extensions'
include Asciidoctor


class XmlDefBlock < Extensions::BlockProcessor
  use_dsl
  named :def
  on_contexts :open, :paragraph, :example, :listing, :sidebar, :pass

  def process parent, reader, attrs
    # Add pass characters here to prevent html character replacements for < > tags
    pass = "+++"
    attrs['name'] = 'definition'
    attrs['caption'] = 'Definition: '
    
    # downcase the title and replace spaces with underscores.
    #    Also replacing special HTML entities:
    #    &quot; = "
    #    &amp;  = &
    formatted_title = attrs['title'].downcase.tr(" ", "_").gsub(/&/, '&amp;').gsub(/"/, '&quot;')
    #  Sanitize the unformatted title string
    san_title = attrs['title'].gsub(/&/, '&amp;').gsub(/"/, '&quot;')

    link = "<link linkend=\"#{formatted_title}\">#{san_title}</link></simpara>\n<simpara>"
    anchor = "<anchor xml:id=\"#{formatted_title}\" xreflabel=\"[#{formatted_title}]\"/>"
    def_prefix = "<emphasis role=\"strong\">Definition:</emphasis>"

    # concatenate all generated lines and prepend before the included 
    #   definition block content
    concat_lines = reader.lines.unshift(pass, anchor, def_prefix, link, pass)

    create_block parent, :admonition, concat_lines, attrs, content_model: :compound 
  end
end
