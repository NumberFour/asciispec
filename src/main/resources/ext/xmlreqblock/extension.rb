require 'asciidoctor/extensions'
include Asciidoctor


class XmlReqBlock < Extensions::BlockProcessor
  use_dsl
  named :req
  on_contexts :open, :paragraph, :example, :listing, :sidebar, :pass
  name_positional_attributes 'number', 'version'

  def process parent, reader, attrs
    # Add pass characters here to prevent html character replacements for < > tags
    pass = "+++"
    attrs['name'] = 'requirement'
    attrs['caption'] = 'Requirement: '
    id = attrs['id']

    begin
      # downcase the title and replace spaces with underscores.
      #    Also replacing special HTML entities:
      #    &quot; = "
      #    &amp;  = &
      downcased_title = attrs['title'].downcase.tr(" ", "_").gsub("\"", "&quot;")
      san_title = attrs['title'].gsub(/&/, '&amp;')    
    
    rescue Exception => msg
      puts msg
      # If no title exists on the Req block, throw an exception
      puts "[ERROR] Requirement block title missing"  
    end



    anchor = "<anchor xml:id=\"Req-#{id}\" xreflabel=\"[Req-#{id}]\"/>"
    req_prefix = "<emphasis role=\"strong\">Requirement: #{id}:</emphasis>"
    link = "<link linkend=\"Req-#{id}\">#{san_title}</link> (ver. #{attrs['version']})</simpara>
    <simpara>"
    
    # concatenate all generated lines and prepend before the original content
    concat_lines = reader.lines.unshift(pass, anchor, req_prefix, link, pass)

    create_block parent, :admonition, concat_lines, attrs, content_model: :compound 
  end

end
