// shorthand to vertically position elements
#let b(dy, content, size: none, weight: none) = {
    set text(size: size) if size != none
    set text(weight: weight) if weight != none
    place(dy: dy, text(content))
}

#let todo(body, big_text: 40pt, small_text: 15pt, gap: 2mm) = {
    set text(fill: black, size: small_text, weight: "bold")
    block(
      fill: yellow,
      stroke: 1pt + rgb("#e0e0e0"),
      width: 100%,
      radius: 4pt,
      inset: 10pt,
      [TODO: #body 
    #place()[    
      #set text(size: 0pt)
      #figure(kind: "todo", supplement: "", caption: body, [])
    ]])
}

//Function to insert TODOs outline
#let todo_outline = outline(
    title: [TODOs],
    target: figure.where(kind: "todo")
)
