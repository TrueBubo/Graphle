#import "../../../template/shared.typ": *
#import "shared.typ": pill, url-pill, header

#let body(urls: (), tags: (), files: ()) = block(
  width: 100%,
  fill: white,
  inset: (x: 10pt, y: 8pt),
  align(left, stack(
    dir: ttb,
    spacing: 6pt,
    text(fill: luma(20), weight: "bold")[URLs],
    if urls.len() > 0 { stack(dir: ltr, spacing: 6pt, ..urls.map(u => url-pill(u))) },
    text(fill: luma(20), weight: "bold")[Tags],
    stack(dir: ltr, spacing: 6pt, ..tags.map(t => pill(t))),
    v(2pt),
    text(fill: luma(20), weight: "bold")[Files],
    ..files.map(f => pill(f)),
  ))
)

#let main-page-mockup() = figure(
  placement: none,
  caption: [Main page - file detail view],
  box(
    width: 88%,
    stroke: 0.5pt + luma(200),
    radius: 4pt,
    clip: true,
    stack(
      dir: ttb,
      header("detail /Users/johndoe",),
      body(
        urls: ("WebPage | https://example.org/",),
        tags: (
            "user | John Doe",
        ),
        files: (
          "↑ | /Users",
          "Backups = John Doe | /Volumes/Backups/johndoe",
          "Shared | /Shared",
          "↓ | /Users/johndoe/Music",
          "↓ | /Users/johndoe/Documents",
        ),
      ),
    )
  )
)
