#import "shared.typ": seq-diagram

#let sweeper-diagram() = seq-diagram(
  caption: [Background sweep of orphaned database state.],
  actors: ("Sweeper coroutine", "FileRepository", "TagRepository", "Filesystem", "Neo4j"),
  steps: (
    (from: 0, to: 0, label: [wait `sweeper.interval`], kind: "self"),
    (from: 0, to: 1, label: [`findAll()`]),
    (from: 1, to: 4, label: [read all `File` nodes]),
    (from: 4, to: 1, label: [locations], kind: "return"),
    (from: 1, to: 0, label: [locations], kind: "return"),
    (from: 0, to: 3, label: [`Files.exists(location)` per entry]),
    (from: 3, to: 0, label: [boolean], kind: "return"),
    (from: 0, to: 1, label: [`removeFileByLocation` (stale)]),
    (from: 1, to: 4, label: [`DETACH DELETE`]),
    (from: 0, to: 2, label: [`removeOrphanTags`]),
    (from: 2, to: 4, label: [`MATCH (t:Tag) WHERE NOT (t)--() DELETE t`]),
  ),
)
