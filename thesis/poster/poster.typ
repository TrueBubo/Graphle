// ============================================================================
//  Graphle - bachelor thesis poster
//  Frame: 842 x 1185 mm  ·  visible area: 820 x 1163 mm
//  Visual-first layout: diagrams and screenshots carry the story; prose is
//  limited to motivation, goals and conclusion.
// ============================================================================

// ---- palette ---------------------------------------------------------------
#let navy    = rgb("#0b1f4d")
#let blue    = rgb("#2c4fd6")
#let sky     = rgb("#5b7cf0")
#let ink     = rgb("#17223b")
#let muted   = rgb("#5a6b8c")
#let border  = rgb("#cbd5ee")
#let soft    = rgb("#eef2fb")
#let code-bg = rgb("#f3f6fc")
#let amber   = rgb("#c2410c")
#let amber-s = rgb("#fdeadb")
#let red     = rgb("#c0392b")
#let grey    = rgb("#9aa7bf")

// ---- fonts -----------------------------------------------------------------
#let title-font = "Technika"
#let body-font  = "Libertinus Serif"
#let mono       = ("DejaVu Sans Mono", "Menlo")

// ============================================================================
//  Page & base text
// ============================================================================
#set page(
  width: 842mm,
  height: 1185mm,
  margin: 30mm,
  fill: rgb("#fbfcfe"),
)

#set text(font: body-font, size: 31pt, fill: ink, lang: "en")
#set smartquote(enabled: false)
#set par(justify: true, leading: 0.62em, spacing: 0.9em)

#show link: it => text(fill: blue)[#it]
#show raw: set text(font: mono, size: 22pt)

// ============================================================================
//  Helpers
// ============================================================================

// a section card with a coloured title bar
#let card(title, body, pad: 16pt) = block(
  width: 100%,
  breakable: false,
  fill: white,
  stroke: 1pt + border,
  radius: 8pt,
  inset: 0pt,
  above: 0pt, below: 0pt,
)[
  #block(
    width: 100%,
    fill: navy,
    inset: (x: 16pt, y: 10pt),
    radius: (top: 8pt),
  )[
    #text(font: title-font, weight: "bold", size: 38pt, fill: white)[#title]
  ]
  #block(inset: (x: pad, y: pad), width: 100%)[#body]
]

// coloured bullet list
#let feats(..items) = {
  set par(justify: false, leading: 0.55em)
  for it in items.pos() {
    grid(
      columns: (16pt, 1fr),
      column-gutter: 10pt,
      align: (left + horizon, left),
      [#box(circle(radius: 5pt, fill: blue))],
      [#it],
    )
    v(6pt)
  }
}

// small pill
#let pill(body, fill: soft, txt: ink, size: 21pt) = box(
  fill: fill,
  inset: (x: 9pt, y: 5pt),
  radius: 20pt,
)[#text(size: size, fill: txt, font: mono)[#body]]

// caption under a figure
#let cap(body) = align(center)[
  #text(size: 22pt, fill: muted, style: "italic")[#body]
]

// technology logo with its name underneath
#let logo(file, name, w: 19mm) = align(center)[
  #image("assets/logos/" + file + ".svg", width: w, height: 19mm, fit: "contain")
  #v(2pt)
  #text(size: 18pt, fill: muted)[#name]
]

// ---- canvas primitives ------------------------------------------------------
#let nodebox(
  x, y, w, label,
  h: 14mm, fill: white, line: navy, txt: navy,
  dash: none, size: 20pt, weight: "regular", font: mono,
) = place(
  top + left, dx: x, dy: y,
  box(
    width: w, height: h,
    fill: fill,
    stroke: (paint: line, thickness: 1.6pt, dash: dash),
    radius: 7pt,
    inset: (x: 6pt, y: 5pt),
  )[
    #align(center + horizon)[
      #text(font: font, size: size, fill: txt, weight: weight)[#label]
    ]
  ],
)

#let conn(x1, y1, x2, y2, col: sky, th: 2.2pt, dash: none) = place(
  top + left,
  line(start: (x1, y1), end: (x2, y2), stroke: (paint: col, thickness: th, dash: dash)),
)

#let at(x, y, body) = place(top + left, dx: x, dy: y, body)

#let elabel(x, y, t, col: blue) = at(x, y,
  box(fill: white, inset: (x: 4pt, y: 1pt))[
    #text(size: 19pt, style: "italic", fill: col)[#t]
  ]
)

// arrow pointing down, ending at y + len
#let arrow-down(x, y, len, col: sky) = {
  conn(x, y, x, y + len - 4mm, col: col)
  at(x - 3.5mm, y + len - 5mm, polygon(fill: col, (0mm, 0mm), (7mm, 0mm), (3.5mm, 5mm)))
}

// app screenshot in a light frame
#let shot(path) = block(
  radius: 6pt,
  clip: true,
  stroke: 1pt + border,
)[#image("assets/shots/" + path, width: 100%)]

// numbered step badge
#let badge(n) = box(
  circle(radius: 12mm, fill: blue)[
    #align(center + horizon)[#text(size: 32pt, fill: white, weight: "bold")[#n]]
  ]
)

// ============================================================================
//  Figure: tree vs. graph
// ============================================================================
#let tree-vs-graph = {
  let panel(x, title, col, body) = {
    at(x, 0mm, box(
      width: 190mm, height: 160mm,
      fill: white, stroke: 1.4pt + border, radius: 10pt,
    ))
    at(x, 0mm, box(width: 190mm, inset: (x: 8pt, y: 7pt))[
      #align(center)[#text(size: 24pt, weight: "bold", fill: col)[#title]]
    ])
    at(x, 0mm, box(width: 190mm, height: 160mm)[#body])
  }

  box(width: 400mm, height: 160mm)[
    // ------------------------------------------------ left panel: the tree
    #panel(0mm, "Filesystem today: a tree", muted)[
      #conn(95mm, 46mm, 50mm, 74mm, col: grey)
      #conn(95mm, 46mm, 140mm, 74mm, col: grey)
      #conn(50mm, 88mm, 50mm, 116mm, col: grey)
      #conn(140mm, 88mm, 140mm, 116mm, col: grey, dash: "dashed")

      #nodebox(60mm, 32mm, 70mm, [Photos/], line: grey, txt: muted)
      #nodebox(15mm, 74mm, 70mm, [Holidays/], line: grey, txt: muted)
      #nodebox(105mm, 74mm, 70mm, [People/Anna/], line: grey, txt: muted, size: 18pt)

      #nodebox(10mm, 116mm, 80mm, [beach.jpg], line: navy, fill: soft, weight: "bold")
      #nodebox(100mm, 116mm, 80mm, [beach.jpg], line: grey, txt: grey, dash: "dashed")
      #at(136mm, 93mm, box(fill: white, inset: (x: 3pt, y: 0pt))[#text(size: 52pt, fill: red, weight: "bold")[×]])

      #at(7mm, 138mm, box(width: 176mm)[
        #align(center)[#text(size: 21pt, fill: muted)[one file, exactly one folder]]
      ])
    ]

    // ------------------------------------------------ arrow between panels
    #at(191mm, 70mm, text(size: 48pt, fill: blue)[#sym.arrow.r])

    // ------------------------------------------------ right panel: the graph
    #panel(210mm, "With Graphle: a graph", blue)[
      #conn(95mm, 82mm, 41mm, 52mm)
      #conn(95mm, 82mm, 149mm, 52mm)
      #conn(95mm, 97mm, 95mm, 134mm)

      #nodebox(8mm, 38mm, 66mm, [Holidays/], size: 18pt)
      #nodebox(116mm, 38mm, 66mm, [People/Anna/], size: 18pt)
      #nodebox(60mm, 134mm, 70mm, [Trip blog], size: 18pt)

      #nodebox(55mm, 82mm, 80mm, [beach.jpg], h: 15mm, fill: soft, line: blue, weight: "bold")

      #elabel(18mm, 60mm, "stored-in")
      #elabel(116mm, 60mm, "shows")
      #elabel(97mm, 110mm, "used-in")
      #conn(58mm, 95mm, 48mm, 103mm, col: amber, th: 1.4pt, dash: "dotted")
      #conn(132mm, 95mm, 142mm, 103mm, col: amber, th: 1.4pt, dash: "dotted")
      #at(8mm, 101mm, pill([year = 2024], fill: amber-s, txt: amber, size: 18pt))
      #at(126mm, 101mm, pill([type = photo], fill: amber-s, txt: amber, size: 18pt))
    ]
  ]
}

// ============================================================================
//  Figure: architecture
// ============================================================================
#let architecture = box(width: 272mm, height: 288mm)[
  // ---------------- client
  #nodebox(0mm, 0mm, 272mm, h: 44mm, [], line: border)
  #at(0mm, 0mm, box(width: 272mm, height: 44mm, inset: 9pt)[
    #align(center + horizon)[
      #stack(
        spacing: 5pt,
        text(size: 26pt, weight: "bold", font: body-font, fill: navy)[Desktop client],
        v(3pt),
        text(size: 20pt, font: body-font, fill: muted)[browse · tag · link · query],
      )
    ]
  ])
  #at(222mm, 8mm, image("assets/logos/jetpackcompose.svg", width: 16mm))

  #arrow-down(136mm, 44mm, 34mm)
  #at(144mm, 46mm, text(size: 19pt, fill: blue, font: mono)[GraphQL · REST])
  #at(144mm, 60mm, text(size: 19pt, fill: blue, font: mono)[WebSocket])

  // ---------------- backend
  #nodebox(0mm, 78mm, 272mm, h: 62mm, [], line: blue, fill: soft)
  #at(0mm, 78mm, box(width: 272mm, height: 62mm, inset: 10pt)[
    #align(center + horizon)[
      #stack(
        spacing: 11pt,
        text(size: 26pt, weight: "bold", font: body-font, fill: navy)[GraphleManager],
        text(size: 20pt, font: body-font, fill: muted)[DSL interpreter],
        text(size: 20pt, font: body-font, fill: muted)[lazy sync + background sweeper],
      )
    ]
  ])
  #at(8mm, 86mm, image("assets/logos/springboot.svg", width: 16mm))
  #at(247mm, 86mm, image("assets/logos/graphql.svg", width: 16mm))

  // ---------------- spine to the three stores
  #conn(16mm, 140mm, 16mm, 268mm)
  #let store(y, logo-file, logo-w, title, sub) = {
    conn(16mm, y + 20mm, 40mm, y + 20mm)
    nodebox(40mm, y, 232mm, h: 40mm, [], line: border)
    at(40mm, y, box(width: 232mm, height: 40mm, inset: (x: 9pt, y: 8pt))[
      #align(horizon)[#grid(
        columns: (24mm, 1fr),
        column-gutter: 9pt,
        align: (center + horizon, left + horizon),
        [#image("assets/logos/" + logo-file + ".svg", width: logo-w)],
        [#stack(
          spacing: 7pt,
          text(size: 23pt, weight: "bold", font: body-font, fill: navy)[#title],
          text(size: 19pt, font: body-font, fill: muted)[#sub],
        )],
      )]
    ])
  }
  #store(148mm, "folder", 19mm, [Your filesystem], [files stay where they are])
  #store(200mm, "neo4j", 17mm, [Neo4j], [tags + relationships only])
  #store(252mm, "valkey", 19mm, [Valkey], [autocomplete index])
]

// ============================================================================
//  Figure: query anatomy
// ============================================================================
#let q(body, fill: white, txt: navy) = box(
  fill: fill, inset: (x: 7pt, y: 6pt), radius: 5pt,
)[#text(font: mono, size: 29pt, fill: txt, weight: "bold")[#body]]

#let query-anatomy = context {
  let gap = 2pt
  let w-find = measure(q([find], txt: muted)).width
  let w-file = measure(q([(tagName = "topic")], fill: soft, txt: blue)).width
  let w-rel  = measure(q([\[name = "cites"\]], fill: amber-s, txt: amber)).width
  let w-any  = measure(q([()], fill: soft, txt: blue)).width

  // centre of every scope, measured from the left edge of the query
  let c-file = w-find + gap + w-file / 2
  let c-rel  = w-find + gap + w-file + gap + w-rel / 2
  let c-any  = w-find + gap + w-file + gap + w-rel + gap + w-any / 2

  // a caption centred underneath the scope it points at
  let lab(cx, y, body, col) = place(top + left, dx: cx - 60mm, dy: y,
    box(width: 120mm)[#align(center)[#text(size: 22pt, fill: col)[#body]]])

  box(width: 100%, height: 84mm)[
    #at(0mm, 0mm, stack(
      dir: ltr,
      spacing: gap,
      q([find], txt: muted),
      q([(tagName = "topic")], fill: soft, txt: blue),
      q([\[name = "cites"\]], fill: amber-s, txt: amber),
      q([()], fill: soft, txt: blue),
    ))

    #conn(c-file, 19mm, c-file, 29mm, col: blue, th: 1.6pt)
    #conn(c-rel, 19mm, c-rel, 45mm, col: amber, th: 1.6pt)
    #conn(c-any, 19mm, c-any, 61mm, col: blue, th: 1.6pt)

    #lab(c-file, 29mm, [files carrying that tag], blue)
    #lab(c-rel, 45mm, [follow this relationship], amber)
    #lab(c-any, 61mm, [to any file], blue)
  ]
}

// ============================================================================
//  HEADER
// ============================================================================
#align(center)[
  #box(baseline: 11mm)[#image("assets/graphle-logo.svg", width: 46mm)]
  #h(10mm)
  #text(font: title-font, weight: "bold", size: 108pt, fill: navy)[Graphle]

  #v(0mm)
  #text(font: title-font, weight: "regular", size: 52pt, fill: ink)[
    Graph-oriented file management system
  ]

  #v(4mm)
  #text(size: 32pt, style: "italic", fill: muted)[
    Organize your files the way you think.
  ]

  #v(5mm)
  #text(size: 30pt)[*Filip Bubák* #h(9mm) · #h(9mm) Supervisor: *Ing. Pavel Koupil, Ph.D.*]
]

#v(5mm)
#line(length: 100%, stroke: 3pt + navy)
#v(4mm)

// ============================================================================
//  ROW 1 - motivation & goals  |  why a graph
// ============================================================================
#grid(
  columns: (0.84fr, 1.16fr),
  column-gutter: 16mm,
  align: top,

  [
    #card("Motivation")[
      A filesystem is a tree: every file lives in exactly one folder. Yet a single
      photo belongs to a holiday, a person and a project all at the same time. Graphle
      leaves files where they are and adds the connections a tree cannot express.
    ]

    #v(8mm)

    #card("Goals")[
      #feats(
        [*Graph data model* over the existing filesystem],
        [*Query language* for structure, tags and relationships],
        [*Backend* keeping the graph in sync with a live filesystem],
        [*Remote access* to a backend on another machine],
        [*Graphical client* for browsing, editing and querying],
      )
    ]
  ],

  [
    #card("Why a graph?")[
      #align(center)[#tree-vs-graph]
      #v(4pt)
      #cap[One photo, three contexts, plus tags no folder name could hold.]
    ]
  ],
)


// ============================================================================
//  ROW 2 - how it works: architecture + a typical session
// ============================================================================
#card("How it works")[
  #grid(
    columns: (272mm, 1fr),
    column-gutter: 20mm,
    align: (top, top),

    [
      #architecture
      #v(4pt)
      #cap[Only tags and relationships are stored; \ files themselves never move.]

      #v(10mm)
      #block(
        width: 100%,
        fill: white,
        stroke: 1pt + border,
        radius: 8pt,
        inset: (x: 10pt, y: 12pt),
      )[
        #align(center)[#text(size: 21pt, fill: muted, style: "italic")[built with]]
        #v(7pt)
        #grid(
          columns: (1fr,) * 4,
          row-gutter: 12pt,
          logo("kotlin", "Kotlin"),
          logo("jetpackcompose", "Compose MP"),
          logo("springboot", "Spring Boot"),
          logo("graphql", "GraphQL"),
          logo("neo4j", "Neo4j"),
          logo("valkey", "Valkey"),
          logo("docker", "Docker"),
          logo("websocket", "WebSocket"),
        )
      ]
    ],

    [
      #let step(n, label, path) = grid(
        columns: (28mm, 1fr),
        column-gutter: 8pt,
        align: (center + top, left),
        [#badge(n)],
        [
          #text(size: 24pt, weight: "bold", fill: navy)[#label]
          #v(4pt)
          #shot(path)
        ],
      )

      #set block(width: 100%)
      #step([1], [Open a file: its tags, links and relationships], "file-detail.png")
      #v(3mm)
      #step([2], [Ask for a tag: every file that carries it], "tag-view.png")
      #v(3mm)
      #step([3], [Follow a relationship: jump to what it points at], "relationship-filter.png")
      #v(5pt)
      #cap[The command line always shows the query behind the view.]
    ],
  )
]


// ============================================================================
//  ROW 3 - query language & tech  |  conclusion, thanks, links
// ============================================================================
#grid(
  columns: (1.12fr, 0.88fr),
  column-gutter: 16mm,
  align: top,

  [
    #card("Query language")[
      #query-anatomy

      #grid(
        columns: (auto, 1fr),
        column-gutter: 12pt,
        row-gutter: 9pt,
        align: (right + horizon, left + horizon),
        [#text(size: 22pt, fill: muted)[tag a file]],
        [#pill([addTag "the-matrix.mkv" "year" "1999"], fill: code-bg, size: 24pt)],
        [#text(size: 22pt, fill: muted)[link two files]],
        [#pill([addRel "questions.md" "experiment.md" "motivates"], fill: code-bg, size: 24pt)],
      )

      #v(9pt)
      #stack(
        dir: ltr,
        spacing: 6pt,
        pill([=]), pill([!=]), pill([<>]), pill([<]), pill([<=]), pill([>]), pill([>=]),
        pill([AND]), pill([OR]),
        pill([\[DESC\]], fill: amber-s, txt: amber), pill([\[PRED\]], fill: amber-s, txt: amber),
      )
    ]

  ],

  [
    #card("Conclusion")[
      Files can stay in the filesystem and still be organised and explored as a graph.
      The result is a working cross-platform application: a graph data model, a query
      DSL, a backend exposing GraphQL, REST and WebSocket interfaces, and a desktop
      client.
    ]
  ],
)


// ============================================================================
//  ROW 4 - acknowledgements  |  code & contact
// ============================================================================
#grid(
  columns: (1.12fr, 0.88fr),
  column-gutter: 16mm,
  align: top,

  [
    #card("Acknowledgements", pad: 14pt)[
      #text(size: 27pt)[
        Thanks to my supervisor, Pavel Koupil, for his guidance throughout this thesis.
      ]
    ]
  ],

  [
    #card("Code & contact", pad: 10pt)[
      #grid(
        columns: (33mm, 1fr),
        column-gutter: 9mm,
        align: (center + horizon, left + horizon),
        [
          #box(fill: white, stroke: 1pt + border, radius: 8pt, inset: 4pt)[
            #image("assets/repo-qr.svg", width: 28mm)
          ]
        ],
        [
          #set text(size: 23pt)
          #grid(
            columns: (auto, 1fr),
            column-gutter: 10pt,
            row-gutter: 4pt,
            align: left + horizon,
            [*GitHub*], [#link("https://github.com/TrueBubo/Graphle")[github.com/TrueBubo/Graphle]],
            [*Email*], [#link("mailto:bubak.filip@pm.me")[bubak.filip\@pm.me]],
            [*LinkedIn*], [#link("https://www.linkedin.com/in/filip-bubak")[linkedin.com/in/filip-bubak]],
          )
        ],
      )
    ]
  ],
)
