#import "../../../template/shared.typ": *
#import "shared.typ": pill, header

#let body(files: ()) = block(
  width: 100%,
  fill: white,
  inset: (x: 10pt, y: 8pt),
  align(left, stack(
    dir: ttb,
    spacing: 6pt,
    text(fill: luma(20), weight: "bold")[Tagged files],
    stack(dir: ttb, spacing: 4pt, ..files.map(f => pill(f))),
  ))
)

#let files-with-tag-page-mockup() = figure(
  placement: none,
  caption: [Main page - files with tag view],
  box(
    width: 88%,
    stroke: 0.5pt + luma(200),
    radius: 4pt,
    clip: true,
    stack(
      dir: ttb,
      header("tag \"project\""),
      body(
        files: (
          "project=graphle | /Users/johndoe/Programming/graphle",
          "project=graphle | /Users/johndoe/Documents/design-notes.md",
          "project=thesis | /Users/johndoe/Documents/thesis",
          "project=thesis | /Users/johndoe/Documents/references.bib",
        ),
      ),
    )
  )
)
