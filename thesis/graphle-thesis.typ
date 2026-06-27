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

  // set to true if generating a PDF for print (shifts page layout, turns code blocks grayscale, correctly aligns odd/even pages,...)
  print: false,

  show-todos: true,

  abstract-en: [
    Operating systems organize files in directory trees, which forces each file into
    one primary location and makes it difficult to express multiple semantic
    associations. This thesis presents Graphle, a graph-oriented file management
    system that extends an existing filesystem with tags and typed relationships
    between files and folders. Files remain stored in their original directories,
    while semantic metadata is kept in a labeled property graph. The result of this
    thesis is a working application that supports graphical and command-based
    interaction, preserves compatibility with ordinary filesystem tools, and
    demonstrates the feasibility of graph-based file organization.
  ],

  abstract-cz: [
    Operační systémy organizují soubory do adresářových stromů, což každý soubor omezuje na
    jedno primární umístění a ztěžuje vyjádření více sémantických
    vztahů. Tato práce představuje Graphle, grafově orientovaný systém pro správu souborů,
    který rozšiřuje stávající souborový systém o značky a typované vztahy
    mezi soubory a složkami. Soubory zůstávají uloženy ve svých původních adresářích,
    zatímco sémantická metadata jsou uchovávána v grafu vlastností s popisky. Výsledkem této
    práce je funkční aplikace, která podporuje grafickou i příkazovou
    interakci, zachovává kompatibilitu s běžnými nástroji pro práci se souborovým systémem a
    dokazuje proveditelnost grafové organizace souborů.
  ],

  keywords-en: [
    file management, filesystem, labeled property graph, file tagging,
    semantic relationships, domain-specific language
  ],

  keywords-cz: [
    správa souborů, souborový systém, graf vlastností s popisky,
    značkování souborů, sémantické vztahy, doménově specifický jazyk
  ],

  acknowledgement: [
    I would like to thank my supervisor, Pavel Koupil, for his guidance and
    support during my work on this thesis.
  ],
)

#include "chapters/introduction.typ"

#include "chapters/analysis/analysis.typ"

#include "chapters/landscape.typ"

#include "chapters/design/design.typ"

#include "chapters/developer/developer.typ"

#include "chapters/user/user.typ"

#include "chapters/conclusion.typ"

#bibliography("bibliography.bib")


// all h1 headings from here on are appendices
#show: start-appendix

#include "chapters/vocabulary.typ"
