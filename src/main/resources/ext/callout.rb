Asciidoctor::Extensions.register do
  inline_macro do
    named :call
    process do |parent, target, attrs|
      Asciidoctor::Inline.new(parent, :callout, target.to_i).convert
    end
  end
end
