// ============================================================================
//  Graphle — bachelor thesis poster
//  Frame: 842 x 1185 mm  ·  visible area: 820 x 1163 mm
//  Page is set to the full frame size; a 30 mm margin keeps all content
//  comfortably inside the visible area.
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

// ---- fonts -----------------------------------------------------------------
#let title-font = "Technika"
#let body-font  = "Libertinus Serif"

// ============================================================================
//  Page & base text
// ============================================================================
#set page(
  width: 842mm,
  height: 1185mm,
  margin: 30mm,
  fill: rgb("#fbfcfe"),
)

#set text(font: body-font, size: 30pt, fill: ink, lang: "en")
#set par(justify: true, leading: 0.62em, spacing: 0.9em)

#show link: it => text(fill: blue)[#it]
#show raw: set text(font: ("DejaVu Sans Mono", "Menlo"), size: 21pt)

// code blocks
#show raw.where(block: true): it => block(
  width: 100%,
  fill: code-bg,
  stroke: (left: 4pt + sky),
  radius: 3pt,
  inset: (x: 12pt, y: 11pt),
  above: 10pt, below: 10pt,
)[#it]

// ============================================================================
//  Helpers
// ============================================================================

// a section card with a coloured title bar
#let card(title, body) = block(
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
    inset: (x: 16pt, y: 12pt),
    radius: (top: 8pt),
  )[
    #text(font: title-font, weight: "bold", size: 40pt, fill: white)[#title]
  ]
  #block(inset: (x: 18pt, y: 16pt), width: 100%)[#body]
]

// coloured bullet list
#let dot = box(baseline: 0pt, circle(radius: 4.5pt, fill: blue))
#let feats(..items) = {
  set par(justify: false, leading: 0.55em)
  for it in items.pos() {
    grid(
      columns: (16pt, 1fr),
      column-gutter: 10pt,
      row-gutter: 0pt,
      align: (left + horizon, left),
      [#box(circle(radius: 5pt, fill: blue))],
      [#it],
    )
    v(9pt)
  }
}

// tag / relationship pill
#let pill(body, fill: soft, txt: ink, stroke: none) = box(
  fill: fill,
  inset: (x: 9pt, y: 5pt),
  radius: 20pt,
  stroke: stroke,
)[#text(size: 20pt, fill: txt, font: ("DejaVu Sans Mono", "Menlo"))[#body]]

// ============================================================================
//  Conceptual graph illustration  (hand-drawn, no external packages)
//  Canvas: 336 mm wide x 150 mm tall.
// ============================================================================
#let node(x, y, label, w: 78mm, size: 20pt) = place(
  top + left, dx: x, dy: y,
  box(
    width: w,
    fill: white,
    stroke: 1.4pt + navy,
    radius: 9pt,
    inset: (x: 10pt, y: 9pt),
  )[
    #align(center)[#text(font: ("DejaVu Sans Mono","Menlo"), size: size, fill: navy)[#label]]
  ],
)

#let edge(x1, y1, x2, y2) = place(
  top + left,
  line(start: (x1, y1), end: (x2, y2), stroke: 2pt + sky),
)

#let elabel(x, y, t) = place(
  top + left, dx: x, dy: y,
  box(fill: rgb("#fbfcfe"), inset: (x: 5pt, y: 2pt))[
    #text(size: 19pt, style: "italic", fill: blue)[#t]
  ],
)

// Application screenshots contain unused space on the right. Scale and clip them
// to keep the useful interface large enough to read from poster distance.
#let app-shot(path) = block(width: 100%, clip: true)[
  #move(dx: -12mm)[#image(path, width: 160%)]
]

#let graph-illustration = align(center)[
  #box(width: 336mm, height: 150mm)[
    // edges (drawn first, hidden under nodes)
    #edge(52mm, 24mm, 168mm, 76mm)  // current thesis -> earlier thesis
    #edge(52mm, 128mm, 168mm, 88mm) // current thesis -> research paper
    #edge(284mm, 80mm, 207mm, 80mm) // current thesis -> local repository


    // edge labels
    #elabel(82mm, 38mm, "inspired-by")
    #elabel(91mm, 108mm, "cites")
    #elabel(218mm, 50mm, "implemented-in")

    // satellite nodes
    #node(0mm,   8mm,   "Theses/graph-query-optimization-in-relational-databases.pdf", w: 100mm, size: 16pt)
    #node(0mm,   118mm, "Papers/recursive-query-processing-in-postgresql.pdf", w: 100mm, size: 16pt)
    #node(238mm, 64mm,  "~/dev/postgresql-graph-query-optimizer/", w: 98mm, size: 16pt)

    // centre node (emphasised)
    #place(top + left, dx: 113mm, dy: 57mm,
      box(
        width: 110mm,
        fill: soft,
        stroke: 2.4pt + blue,
        radius: 10pt,
        inset: (x: 10pt, y: 10pt),
      )[
        #align(center)[
          #text(font: ("DejaVu Sans Mono","Menlo"), size: 17pt, weight: "bold", fill: navy)[Theses/improving-graph-query-performance-in-postgresql.pdf]
        ]
      ],
    )
    // metadata tags on the current thesis
    #place(top + left, dx: 100mm, dy: 94mm,
      pill([tracked-under = gh.com/postgres/postgres/issues/3861], fill: rgb("#e6ecfb"), txt: navy)
    )
    #place(top + left, dx: 130mm, dy: 108mm,
      pill([project = postgresql], fill: rgb("#e6ecfb"), txt: navy)
    )
  ]
]

// ============================================================================
//  HEADER
// ============================================================================
#align(center)[
  #text(font: title-font, weight: "bold", size: 150pt, fill: navy)[Graphle]

  #v(2mm)
  #text(font: title-font, weight: "regular", size: 58pt, fill: ink)[
    Graph-oriented file management system
  ]

  #v(6mm)
  #text(size: 34pt, style: "italic", fill: muted)[
    Organize your files the way you think.
  ]

  #v(9mm)
  #text(size: 32pt)[*Filip Bubák*]
  #v(2mm)
  #text(size: 32pt)[Supervisor: *Ing. Pavel Koupil, Ph.D.*]
]

#v(9mm)
#line(length: 100%, stroke: 3pt + navy)
#v(9mm)

// ============================================================================
//  BODY — two columns
// ============================================================================
#grid(
  columns: (1fr, 1fr),
  column-gutter: 22mm,
  align: top,

  // ------------------------------------------------------------------ LEFT
  [
    #card("Motivation")[
      Operating systems store files in a strict tree of directories, where every
      file lives in exactly one place. Yet a single photograph may belong to a
      holiday, a person, and a project at once. Associations like this a tree simply cannot
      express. Existing tools bridge this gap only partially, and rarely let users
      freely connect arbitrary files with typed relationships and tags across the
      whole filesystem in an easily queryable way.
    ]

    #v(20mm)

    #card("Goals")[
      #feats(
        [*Graph data model* — represent files and folders as nodes in a labeled
         property graph, joined by user-defined typed relationships and tags, while
         staying backward-compatible with the existing filesystem.],
        [*Query language (DSL)* — search files and manipulate their tags and
         relationships by graph structure rather than by path alone.],
        [*Backend* — maintain the graph and keep it consistent with a live filesystem
         modified from outside Graphle.],
        [*Remote access* — connect the graphical client to a remote backend and browse
         and manage its files just as if Graphle was running locally.],
        [*Graphical client* — browse, open, move and delete files, edit tags and
         relationships, and write and run DSL queries with filename autocomplete in one place.],
      )
    ]

    #v(20mm)

    #card("Query language")[
      A compact DSL exposes every graph operation, both inside the GUI and as a
      standalone tool for scripts. Searches are built from scopes: file scopes use
      `( … )`, relationship scopes use `[ … ]`.

      #v(4pt)
      Find every file tagged as a dataset:
      ```text
      find (tagName = "type" AND tagValue = "dataset")
      ```

      Follow a typed relationship to its target files:
      ```text
      find (location = "…/research-questions.md")[name = "motivates"]()
      ```

      Add a key-value tag to a file:
      ```text
      addTag "…/the-matrix.mkv" "year" "1999"
      ```

      Add a typed relationship between two files:
      ```text
      addRel "…/research-questions.md" "…/experiment-plan.md" "motivates"
      ```

      #v(4pt)
      #feats(
        [Operators `= != <> < <= > >=`, combined with `AND` / `OR`.],
        [`[DESC]` and `[PRED]` expose the live filesystem hierarchy — usable even
         for files not yet registered in Graphle.],
      )
    ]

    #v(20mm)

    #card("Conclusion")[
      Graphle shows that files can stay in the existing filesystem while being
      organised and explored as a graph. The result is a working application built
      around a graph data model, a query DSL, a backend exposing GraphQL, REST and
      WebSocket interfaces, and a cross-platform graphical client.
    ]

    #v(20mm)

    #card("Acknowledgements")[
      I would like to thank my supervisor, Pavel Koupil, for his guidance and
      support throughout this thesis.
    ]

    #v(20mm)

    #card("Additional information")[
      #grid(
        columns: (58mm, 1fr),
        column-gutter: 12mm,
        align: (center + horizon, left + horizon),
        [
          #box(
            fill: white, stroke: 1pt + border, radius: 8pt, inset: 8pt,
          )[#image("assets/repo-qr.svg", width: 52mm)]
        ],
        [
          #grid(
            columns: (auto, 1fr),
            column-gutter: 10pt,
            row-gutter: 8pt,
            align: left + horizon,
            [*GitHub*],
            [#link("https://github.com/TrueBubo/Graphle")[github.com/TrueBubo/Graphle]],
            [*Email*],
            [#link("mailto:bubak.filip@pm.me")[#text("bubak.filip@pm.me")]],
            [*LinkedIn*],
            [#link("https://www.linkedin.com/in/filip-bubak")[linkedin.com/in/filip-bubak]],
          )
        ],
      )
    ]
  ],

  // ------------------------------------------------------------------ RIGHT
  [
    #card("How it works")[
      Graphle adds a *semantic graph layer* on top of an existing filesystem. Files
      stay in their original directories and remain usable by ordinary tools. Only
      the user's semantic metadata, *tags* and typed *relationships*, are persisted
      in a labeled property graph. Parent and descendant edges are derived live from
      the filesystem, so no data already maintained by the OS is duplicated.

      #v(8pt)
      #graph-illustration
      #v(4pt)
      #align(center)[#text(size: 21pt, fill: muted)[
        Unlike a tree, one paper can support multiple theses while the work links directly to its implementation.
      ]]

      #v(8pt)
      Files are loaded into the graph lazily on first visit, so startup needs no
      full disk scan, and a background sweeper prunes entries for files that have
      disappeared, keeping the graph consistent after external changes.
    ]

    #v(20mm)

    #card("The application")[
      A cross-platform desktop client organises every operation around the currently
      selected file. The hamburger menu provides Home, Trash, hidden-file and dark-mode
      controls, together with actions for the current file. A synchronised command line
      always shows the DSL query behind the view.

      #v(6pt)
      #stack(
        spacing: 12pt,
        app-shot("assets/file-detail.png"),
        app-shot("assets/tag-view.png"),
        app-shot("assets/relationship-filter.png"),
      )
      #v(4pt)
      #text(size: 30pt, fill: muted)[
        Top to bottom: a file with its tags and relationships · files carrying a
        given tag · results of a relationship traversal.
      ]
    ]
  ],
)
