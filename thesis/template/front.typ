#import "shared.typ": *

#let title-page(
  print,
  show-todos,
  title: "",
  title-cz: "",
  author: (
    name: "",
    email: "",
    url: "",
  ),
  submission-date: datetime.today(),
  bachelor: false,
  supervisor: "",
  faculty: "",
  department: "",
  department-cz: "",
  study-programme: "",
  branch-of-study: "",
) = {
  // render as a separate page
  // inner margin is 8mm due to binding loss, but without
  //  the bent page extra, which is not an issue for the title page

  if show-todos [
    #todo_outline
  ]
  
  let a4-width = 210mm
  let text-width = 140mm
  let margin = (a4-width - text-width) / 2

  let inside-margin = if print {8mm} else {0mm}
  show: page.with(margin: (top: 0mm, bottom: 0mm, inside: (inside-margin + margin), outside: margin))

  set align(center)
  set place(center)
  set text(font: "Technika", weight: "extralight", size: 11pt, fallback: false)


  let thesis-type = if bachelor [ BACHELOR THESIS] else [MASTER THESIS]

  box(inset: (left: 15mm))[
    #b(26.2mm)[
      #image("./res/logo-en.svg", width: 166mm)

    ]
  ]



  b(96mm, size: 20.5pt, weight: "regular")[
    #thesis-type 
  ]

  b(128mm, [
    #text(size: 20.5pt, style: "italic")[#author] \
  ])

  b(152mm, size: 20.5pt, weight: "regular")[
    #title
  ]

  b(195mm)[#department]

  b(226mm)[
    Supervisor of the #lower[#thesis-type]: #supervisor
    #v(0mm)
    Study programme: #study-programme
  ]
  
  b(265mm)[Prague #submission-date.display("[year]")]
}

#let acknowledgement-page(
      acknowledgement: [],
) = page()[
    #acknowledgement
]

#let declaration-page() = b(180mm)[
  I declare that I carried out this bachelor thesis on my own, and only with the
  cited sources, literature and other professional sources. I understand that my
  work relates to the rights and obligations under the Act No. 121/2000 Sb., the
  Copyright Act, as amended, in particular the fact that the Charles University has
  the right to conclude a license agreement on the use of this work as a school work
  pursuant to Section 60 subsection 1 of the Copyright Act.

  #v(5mm)
  In . . . . . . . . . . . . . date . . . . . . . . . . . . . #h(7mm) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
  #v(0mm)
  #h(85mm) 
  Author's signature
]


#let abstract-page(
  meta,
  abstract-en: [],
  abstract-cz: [],
  keywords-en: [],
  keywords-cz: [],
  acknowledgement: [],
) = {
  // render as a separate page; add room at the bottom for TODOs and notes
  show: page.with(margin: (bottom: 0mm))
  
  set heading(outlined: false, bookmarked: false)
  // pretty hacky way to disable the implicit linebreak in my heading style
  show heading: it => {
    show pagebreak: it => {linebreak()}
    block(it)
    //block(it, above: 2pt)
  }

  // no idea why there is a margin here
  v(-30.2pt)
  [
    Title: #meta.title
    #v(2mm)
    Author: #meta.author
    #v(2mm)
    Department: #meta.department
    #v(2mm)
    Supervisor: #meta.supervisor, #meta.department-cz
    #v(2mm)
    Abstract: #abstract-en
    #v(2mm)
    Keywords: #keywords-en

  ]

  v(30pt)
    [
      Název práce: #meta.title-cz
      #v(2mm)
      Autor: #meta.author
      #v(2mm)
      Katedra: #meta.department-cz
      #v(2mm)
      Vedoucí bakalářské práce: #meta.supervisor, #meta.department-cz
      #v(2mm)
      Abstrakt: #abstract-cz
      #v(2mm)
      Klíčová slova: #keywords-cz
    ]
  }
}


#let introduction(
  print,
  meta,
  abstract-en: [],
  abstract-cz: [],
  keywords-en: [],
  keywords-cz: [],
  acknowledgement: [],
) = {
  let args = (
    abstract-en: abstract-en,
    abstract-cz: abstract-cz,
    keywords-en: keywords-en,
    keywords-cz: keywords-cz,
    acknowledgement: acknowledgement
   )

  // hide empty pages from web version
  if print {
    // assignment must be on a single sheet from both sides
    pagebreak(to: "odd")
  } else {
    // Typst cannot embed PDFs, add the assignment separately
  }

  if print {
    pagebreak(to: "odd", weak: true)
  }

  declaration-page()
  acknowledgement-page(acknowledgement: acknowledgement)
  abstract-page(meta, ..args)

  if print {
    // outline should be on the right, but the outline title has a pagebreak
    pagebreak(to: "even")
  }
  outline(depth: 3)

  pagebreak(weak: true)
}
