#import "./template/template.typ": *

#show: template.with(
  meta: (
    title: "Graphle: Graph-oriented file management system",
    title-cz: "Graphle: Grafově orientovaný systém pro správu souborů",
    author: "Filip Bubák",
    submission-date: datetime(year: 2025, month: 1, day: 1),

    // true for bachelor's thesis, false for master's thesis
    bachelor: true,
    supervisor: "Ing. Pavel Koupil, Ph.D.",

    department: "Department of Software Engineering",
    department-cz: "Katedra softwarového inženýrství",
    study-programme: "Programming and Software Development",
  ),

  // set to true if generating a PDF for print (shifts page layout, turns code blocks greyscale, correctly aligns odd/even pages,...)
  print: false,

  show-todos: true,

  abstract-en: [
    #lorem(60)
  ],

  abstract-cz: [
    #lorem(60)
  ],

  keywords-en: [
    #lorem(20)
  ],

  keywords-cz: [
   #lorem(20)
  ],

  acknowledgement: [
    Acknowledgement
    #lorem(30)
    
    #lorem(30)
  ],
)

#include "chapters/introduction.typ"

#include "chapters/landscape.typ"

#include "chapters/analysis/analysis.typ"

#include "chapters/design/design.typ"

#include "chapters/developer/developer.typ"

#include "chapters/user/user.typ"

#include "chapters/conclusion.typ"

#bibliography("bibliography.bib")


// all h1 headings from here on are appendices
#show: start-appendix

#include "chapters/vocabulary.typ"
