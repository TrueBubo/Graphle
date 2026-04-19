#let pill(body) = box(
  stroke: 0.6pt + luma(200),
  radius: 1em,
  inset: (x: 0.7em, y: 0.35em),
  fill: luma(235),
  text(size: 0.8em, fill: luma(30), body)
)

#let url-pill(body) = box(
  stroke: none,
  radius: 1em,
  inset: (x: 0.7em, y: 0.35em),
  fill: rgb("#0a3cdb"),
  text(size: 0.8em, fill: white, weight: "bold", body)
)

#let header(command) = rect(
  width: 100%,
  fill: luma(225),
  inset: 0pt,
  grid(
    columns: (36pt, 1fr),
    rows: 36pt,
    align: horizon,
    rect(
      width: 100%, height: 100%,
      fill: luma(210),
      stroke: none,
      inset: 0pt,
      align(center + horizon, text(fill: luma(50), size: 14pt)[≡])
    ),
    rect(
      width: 100%, height: 100%,
      fill: luma(225),
      stroke: (bottom: 1pt + luma(180)),
      inset: (x: 8pt),
      align(left + horizon, text(fill: luma(40), size: 0.85em, command))
    ),
  )
)
