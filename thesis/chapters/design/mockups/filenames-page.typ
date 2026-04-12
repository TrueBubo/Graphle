#import "../../../template/shared.typ": *
#import "shared.typ": pill, header

#let body(files: ()) = block(
  width: 100%,
  fill: white,
  inset: (x: 10pt, y: 8pt),
  align(left, stack(
    dir: ttb,
    spacing: 6pt,
    text(fill: luma(20), weight: "bold")[Files],
    stack(dir: ttb, spacing: 4pt, ..files.map(f => pill(f))),
  ))
)

#let find-relationships-page-mockup() = figure(
  placement: none,
  caption: [Main page — find results with relationship filter view],
  box(
    width: 88%,
    stroke: 0.5pt + luma(200),
    radius: 4pt,
    clip: true,
    stack(
      dir: ttb,
      header("find (location = /Users/johndoe)[desc]"),
      body(
        files: (
          "↓ | /Users/johndoe/Music",
          "↓ | /Users/johndoe/Documents",
          "↓ | /Users/johndoe/Downloads",
        ),
      ),
    )
  )
)

#let filenames-page-mockup() = figure(
  placement: none,
  caption: [Main page — filenames view],
  box(
    width: 88%,
    stroke: 0.5pt + luma(200),
    radius: 4pt,
    clip: true,
    stack(
      dir: ttb,
      header("find (tagName = Conference)"),
      body(
        files: (
            "/Users/johndoe/Downloads/conf2025-ticket.pdf",
            "/Users/johndoe/Downloads/conf2026-ticket.pdf",
            "/Users/johndoe/Documents/conf2025",
            "/Users/johndoe/Documents/conf2026",
            "/Users/johndoe/Programming/conf2025/examples",
            "/Users/johndoe/Programming/conf2026/examples",
        ),
      ),
    )
  )
)
