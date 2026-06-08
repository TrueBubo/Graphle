#import "shared.typ": seq-diagram

#let add-file-with-tag-diagram() = seq-diagram(
  caption: [Adding a file and attaching a tag.],
  actors: ("GraphleUI", "FileController", "FileService", "TagController", "Neo4j"),
  steps: (
    (from: 0, to: 1, label: [`addFile(location)`]),
    (from: 1, to: 2, label: [`fileService.addFile(location)`]),
    (from: 2, to: 2, label: [`Files.createFile`], kind: "self"),
    (from: 2, to: 4, label: [`MERGE (:File {location})`]),
    (from: 4, to: 2, label: [`File` node], kind: "return"),
    (from: 2, to: 2, label: [`insertFilesToCompleter` (async)], kind: "self"),
    (from: 1, to: 0, label: [`File`], kind: "return"),
    (from: 0, to: 3, label: [`addTagToFile(location, tag)`]),
    (from: 3, to: 4, label: [`MERGE (:Tag); MERGE (:File)-[:HasTag]->(:Tag)`]),
    (from: 4, to: 3, label: [`Tag`], kind: "return"),
    (from: 3, to: 0, label: [`Tag`], kind: "return"),
    (from: 0, to: 1, label: [re-fetch `fileByLocation`]),
  ),
)
