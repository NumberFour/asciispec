require 'asciidoctor/extensions'
include Asciidoctor


class XmlDefBlock < Extensions::BlockProcessor
  use_dsl
  named :def
  on_contexts :open, :paragraph, :example, :listing, :sidebar, :pass

  def process parent, reader, attrs
    # Add pass characters here to prevent the anchor from being processed
    #    as regular adoc content i.e. html character replacements for < > tags
    pass = "+++"
    attrs['name'] = 'definition'
    attrs['caption'] = 'Definition: '
    title = attrs['caption'] + attrs['title']
    anchor = (create_anchor parent, title, type: :link, target: title).convert
    endcontent = reader.lines.unshift(pass, anchor, pass)
    create_block parent, :admonition, endcontent, attrs, content_model: :compound
  end

end
