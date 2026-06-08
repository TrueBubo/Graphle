#import "shared.typ": seq-diagram

#let autocomplete-keystroke-diagram() = seq-diagram(
  caption: [Autocomplete suggestion on a keystroke.],
  actors: ("GraphleUI", "WebSocket", "DSLAutoCompleter", "FilenameCompleter", "Cache", "Valkey"),
  steps: (
    (from: 0, to: 1, label: [prefix frame on `/ws`]),
    (from: 1, to: 2, label: [`complete(prefix)`]),
    (from: 2, to: 2, label: [classify command], kind: "self"),
    (from: 2, to: 3, label: [`lookup(prefix, limit)`]),
    (from: 3, to: 4, label: [cache read]),
    (from: 4, to: 3, label: [hit / miss], kind: "return"),
    (from: 3, to: 5, label: [on miss: `HGETALL` / `GET` / `EXPIRE`]),
    (from: 5, to: 3, label: [values], kind: "return"),
    (from: 3, to: 3, label: [`filenameDFS`], kind: "self"),
    (from: 3, to: 2, label: [suggestions], kind: "return"),
    (from: 2, to: 1, label: [JSON array], kind: "return"),
    (from: 1, to: 0, label: [frame to client], kind: "return"),
  ),
)
