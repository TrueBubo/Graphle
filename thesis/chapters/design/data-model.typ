#import "../../template/shared.typ": *

== Data Model

The application works with a graph-oriented extension of the user's existing filesystem.
The filesystem itself remains outside the application data model: it stores file contents, permissions, timestamps, and the directory hierarchy.
Graphle stores only the #link(label("voc_metadata"))[metadata] needed for graph navigation and derives the rest from the live filesystem when a query is executed.

#figure(
  placement: none,
  image("data-model/graph-model.svg", width: 55%),
  caption: [Graphle graph data model]
) <graphle-data-model>

The @graphle-data-model contains two node types and two edge types.
`File` is the central node type.
Its `location` attribute is the absolute path on the machine where `GraphleManager` runs and acts as the logical identifier used by the API, DSL, and UI.
The database does not duplicate the file's binary content or operating-system metadata.
This keeps the model compatible with existing filesystem tools and supports #link(label("voc_lazy_loading"))[lazy loading] (F1, F9).

`Tag` represents a reusable categorization label.
It has a required `name` and an optional `value`.
The value allows both simple tags, such as `project`, and key-value tags, such as `priority = high`.
A file is connected to a tag by a `HasTag` edge.
Tags that are no longer connected to any file can be removed without changing the filesystem.

`Relationship` represents a semantic #link(label("voc_relationship"))[relationship] between two files or folders.
It is stored as a directed edge from one `File` node to another and contains a required `name` and an optional `value`.
The direction is part of the model, so a relationship from `A` to `B` does not imply the reverse relationship.
When a user requests a bidirectional relationship, the application represents it as two directed `Relationship` edges with the same attributes.

The filesystem hierarchy is intentionally not persisted as graph metadata.
Parent and descendant relations are computed from the current filesystem state and returned together with stored semantic relationships.
This means that moving or deleting a file outside Graphle does not immediately corrupt a stored hierarchy.
The next query or background sweep can reconcile the graph with the actual filesystem.

The public API exposes `File` as an aggregate view containing its `location`, `tags`, and visible `connections`.
Additional response types, such as `FilesByTagResponse` and DSL responses, are transport shapes rather than independent domain entities.
They exist so clients can display query results and mutation outcomes without introducing new persisted concepts into the data model.
