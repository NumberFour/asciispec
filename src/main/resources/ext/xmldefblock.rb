RUBY_ENGINE == 'opal' ?
  (require 'xmldefblock/extension') :
  (require_relative 'xmldefblock/extension')

Asciidoctor::Extensions.register do
  if (@document.basebackend? 'docbook')
    block XmlDefBlock
  end
end
