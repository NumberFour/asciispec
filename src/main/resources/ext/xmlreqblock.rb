RUBY_ENGINE == 'opal' ?
  (require 'xmlreqblock/extension') :
  (require_relative 'xmlreqblock/extension')

Asciidoctor::Extensions.register do
  if (@document.basebackend? 'docbook')
    block XmlReqBlock
  end
end
