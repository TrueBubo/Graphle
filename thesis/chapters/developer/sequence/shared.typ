// Shared primitives for sequence diagrams.
// Each diagram is a grid of columns (one per actor / lifeline) and rows (one per
// step). Steps are drawn as horizontal arrows between columns with the message
// label rendered above the arrow.

#let _actor-box(name) = rect(
  width: 100%,
  fill: luma(225),
  stroke: 0.5pt + luma(170),
  inset: 6pt,
  align(center, text(weight: "bold", size: 0.85em, name)),
)

#let _empty-lifeline(height: 8pt) = box(
  width: 100%,
  height: height,
  align(center, line(
    length: 100%,
    angle: 90deg,
    stroke: (paint: luma(180), thickness: 0.5pt, dash: "dashed"),
  )),
)

// Draws the body of a single step.
//   from, to  — 0-based column indices of the actors involved
//   label     — message text shown above the arrow
//   kind      — "call"   (solid arrow, default),
//               "return" (dashed arrow),
//               "self"   (self-message on a single actor)
//   total     — total number of actors in the diagram
#let step(from: 0, to: 0, label: "", kind: "call", total: 2) = {
  let lo = calc.min(from, to)
  let hi = calc.max(from, to)
  let is-self = (from == to)
  let dir-right = (to >= from)
  let dash = if kind == "return" { "dashed" } else { "solid" }

  let head-left = if dir-right { "" } else { "◀" }
  let head-right = if dir-right { "▶" } else { "" }

  let self-glyph = "↺"

  let body = if is-self {
    // Self-call: small loop glyph rendered in-column with the label to the right
    align(left + horizon, grid(
      columns: (0.9em, 1fr),
      gutter: 3pt,
      text(size: 0.85em, self-glyph),
      text(size: 0.7em, label),
    ))
  } else {
    // Label above, arrow below. Leave ~1.2em slack on either side so the
    // arrowheads never extend past the enclosing figure box.
    align(center + bottom, stack(dir: ttb, spacing: 2pt,
      text(size: 0.7em, label),
      pad(x: 0.6em, stack(dir: ltr, spacing: 0pt,
        box(width: 0.7em, align(left + horizon, text(size: 0.9em, head-left))),
        box(width: 1fr, align(horizon, line(
          length: 100%,
          stroke: (paint: black, thickness: 0.55pt, dash: dash),
        ))),
        box(width: 0.7em, align(right + horizon, text(size: 0.9em, head-right))),
      )),
    ))
  }

  let cells = ()
  // Empty lifelines left of the arrow
  for _ in range(lo) { cells.push(_empty-lifeline()) }
  // The arrow / self-call cell spans from lo to hi (inclusive)
  cells.push(grid.cell(colspan: hi - lo + 1, box(
    width: 100%,
    height: 22pt,
    body,
  )))
  // Empty lifelines right of the arrow
  for _ in range(total - hi - 1) { cells.push(_empty-lifeline()) }

  cells
}

// Renders a full sequence diagram.
//   actors — list of actor names, left to right
//   steps  — list of step entries. Each entry is a dictionary with keys:
//            from, to, label, and optional kind.
//   caption — figure caption
#let seq-diagram(actors: (), steps: (), caption: "") = figure(
  placement: none,
  caption: caption,
  kind: image,
  supplement: [Figure],
  block(
    width: 100%,
    inset: (x: 8pt, y: 4pt),
    stroke: 0.5pt + luma(200),
    radius: 3pt,
    stack(
      dir: ttb,
      spacing: 0pt,
      // Actor header row
      grid(
        columns: (1fr,) * actors.len(),
        gutter: 4pt,
        ..actors.map(_actor-box)
      ),
      v(2pt),
      // Step rows
      ..steps.map(s => grid(
        columns: (1fr,) * actors.len(),
        gutter: 4pt,
        ..step(
          from: s.from,
          to: s.to,
          label: s.label,
          kind: s.at("kind", default: "call"),
          total: actors.len(),
        )
      )),
    ),
  ),
)
