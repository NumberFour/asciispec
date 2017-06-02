RUBY_ENGINE == 'opal' ?
  (require 'todo/extension') :
  (require_relative 'todo/extension')

Extensions.register do
  block TodoBlock
  docinfo_processor TodoBlockDocinfo
end
