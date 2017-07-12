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
    #    Also replacing double quotes with single quotes to stop xml from breaking
    #    in the case where the title contains a quote symbol
    formatted_title = attrs['title'].downcase.tr(" ", "_").tr("\"", "'")

    # TODO try and use the built-in behaviour of generating anchors:
    #    link = (create_anchor parent, attrs['title'], type: :link, target: attrs['title']).render
    #    not working due to missing 'linkend' property.  
    link = "<link linkend=\"#{formatted_title}\">#{attrs['title']}</link></simpara>\n<simpara>"
    anchor = "<anchor xml:id=\"#{formatted_title}\" xreflabel=\"[#{formatted_title}]\"/>"
    def_prefix = "<emphasis role=\"strong\">Definition:</emphasis>"

    # concatenate all generated lines and prepend before the included 
    #   definition block content
    concat_lines = reader.lines.unshift(pass, anchor, def_prefix, link, pass)

    create_block parent, :admonition, concat_lines, attrs, content_model: :compound 
  end
end
