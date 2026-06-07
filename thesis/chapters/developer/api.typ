#import "../../template/shared.typ": *
== API Documentation

`GraphleManager` exposes three interfaces on the same HTTP port:
GraphQL at `/graphql` for structured CRUD operations,
REST under `/dsl` and `/download` for the command-line DSL client and for file transfers,
and a WebSocket at `/ws` for real-time autocomplete.
All three share the same Spring process and the same security model.

=== GraphQL

The schema is declared in `GraphleManager/src/main/resources/graphql/schema.graphqls` and is the authoritative source for every type and field.
GraphiQL is enabled in `application.properties`, so the schema can be explored interactively at `http://<host>:5824/graphiql`.

#table(
  columns: (auto, 1fr),
  align: (left, left),
  stroke: 0.4pt + luma(160),
  table.header([*Query*], [*Purpose*]),
  [`fileByLocation(location, showHiddenFiles)`], [File detail with tags and all live neighbours (parent, descendants, custom connections).],
  [`fileType(location)`], [Returns `File` or `Directory`, or `null` if the path is not present.],
  [`tagsByFileLocation(location)`], [Lists every tag attached to the given file.],
  [`filesByTag(tagName)`], [Reverse lookup from tag to the files that carry it.],
  [`filesFromFileByRelationship(fromLocation, relationshipName)`], [Files reachable from `fromLocation` via a named relationship (custom or hierarchical).],
  [`optionsByDslPrefix(dslPrefix, limit)`], [Autocomplete suggestions for a DSL prefix; offered over GraphQL as an alternative to the WebSocket.],
  [`entriesFromDSL(dslCommand, limit)`], [Executes a DSL command from GraphQL and returns an `Entries { entryTypeName, identifiers[] }` tuple.],
)

#table(
  columns: (auto, 1fr),
  align: (left, left),
  stroke: 0.4pt + luma(160),
  table.header([*Mutation*], [*Purpose*]),
  [`addFile(location)`], [Creates the file on disk, adds the `File` node, schedules an insert into the autocomplete trie.],
  [`removeFile(location)`], [Removes the file from disk and `DETACH DELETE`s its node.],
  [`moveFile(locationFrom, locationTo)`], [Moves the file on disk and updates the node's `location` property.],
  [`addTagToFile(location, tag)`], [`MERGE`s the tag node and the `HasTag` edge.],
  [`removeTag(location, tag)`], [Drops the `HasTag` edge and garbage-collects the tag node if it becomes orphaned.],
  [`addConnection(connection)`], [Creates a named edge between two `File` nodes.],
  [`removeConnection(connection)`], [Removes a named edge between two `File` nodes.],
)

The `File` type embeds `tags: [Tag!]!` and `connections: [Connection!]!` as nested fields, so a client can request the complete detail in a single round trip.
Inputs `TagInput` and `ConnectionInput` mirror their output types; `ConnectionInput.bidirectional` is non-nullable on input so callers must decide whether the new edge should be traversed both ways.

=== REST

`POST /dsl` accepts `{ "command": "<dsl source>" }` and returns `{ "type": ResponseType, "responseObject": [String] }`.
`ResponseType` is one of `FILENAMES`, `CONNECTIONS`, `FILE`, `TAG`, `SUCCESS`, or `ERROR`. The payload shape depends on the type:

- `FILENAMES` — absolute paths.
- `CONNECTIONS` — JSON-serialised `Connection` records.
- `FILE` / `TAG` — JSON-serialised `File` / `TagForFile` records.
- `SUCCESS` — empty list; used by mutating commands such as `addFile`, `addRel`, `addTag`.
- `ERROR` — a single human-readable message.

`GET /download?location=<absolute path>` streams the file contents with a detected `Content-Type`, served by `FileDownloadController`.

=== WebSocket

`/ws` carries the autocomplete protocol used by the GUI.
The client opens a single long-lived session, which is able to reconnect with exponential back-off if it drops, and sends the current DSL prefix on every keystroke.
The server answers with a JSON array of suggestions produced by `DSLAutoCompleter`.
Keeping the connection persistent avoids a TCP handshake per keystroke, which is what makes the 250 ms budget in Q2.1 achievable.

=== Authentication

No authentication is performed on any of the three interfaces.
GraphleManager is intended to run on the user's own machine or on a trusted LAN host they control, which matches the remote-access requirement (Q1.3) without exposing the filesystem to untrusted callers.
Adding authentication would require a uniform layer across GraphQL, REST, and the WebSocket handshake and is deliberately out of scope.
