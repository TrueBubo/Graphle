#import "../../template/shared.typ": *
== #voc("api") Documentation

`GraphleManager` exposes three interfaces on the same #voc("http") port:
#voc("graphql") at `/graphql` for structured CRUD operations,
#voc("rest") under `/dsl` and `/download` for the command-line DSL client and for file transfers,
and a #voc("websocket") at `/ws` for real-time autocomplete.
All three share the same Spring process and the same security model.

=== GraphQL

The schema is declared in `GraphleManager/src/main/resources/graphql/schema.graphqls` and is the authoritative source for every type and field.
GraphiQL is enabled in `application.properties`, so the schema can be explored interactively at `http://<host>:5824/graphiql`.

#table(
  columns: (15em, 1fr),
  align: (left, left),
  stroke: 0.4pt + luma(160),
  table.header([*Query*], [*Purpose*]),
  [`fileByLocation(location, showHiddenFiles)`], [File detail with #voc("tag", text: "tags") and all live #voc("neighbor", text: "neighbors") (parent, descendants, custom #voc("connection", text: "connections")).],
  [`fileType(location)`], [Returns `File` or `Directory`, or `null` if the path is not present.],
  [`tagsByFileLocation(location)`], [Lists every #voc("tag") attached to the given file.],
  [`filesByTag(tagName)`], [Reverse lookup from #voc("tag") to the files that carry it.],
  [`filesFromFileByRelationship(fromLocation, relationshipName)`], [Files reachable from `fromLocation` through a named #voc("relationship") (custom or hierarchical).],
  [`optionsByDslPrefix(dslPrefix, limit)`], [Autocomplete suggestions for a #voc("dsl") prefix, offered over #voc("graphql") as an alternative to the #voc("websocket").],
  [`entriesFromDSL(dslCommand, limit)`], [Executes a #voc("dsl") command from #voc("graphql") and returns an `Entries { entryTypeName, identifiers[] }` tuple.],
)

#table(
  columns: (auto, 1fr),
  align: (left, left),
  stroke: 0.4pt + luma(160),
  table.header([*Mutation*], [*Purpose*]),
  [`addFile(location)`], [Creates the file on disk, adds the `File` node, schedules an insert into the autocomplete #voc("trie").],
  [`removeFile(location)`], [Removes the file from disk and deletes its node along with all its edges from the graph.],
  [`moveFile(locationFrom, locationTo)`], [Moves the file on disk and updates the stored path.],
  [`addTagToFile(location, tag)`], [Creates or updates the #voc("tag") node and its link to the file.],
  [`removeTag(location, tag)`], [Removes the #voc("tag") link and garbage-collects the #voc("tag") node if it becomes orphaned.],
  [`addConnection(connection)`], [Creates a named edge between two `File` nodes.],
  [`removeConnection(connection)`], [Removes a named edge between two `File` nodes.],
)

The `File` type embeds `tags: [Tag!]!` and `connections: [Connection!]!` as nested fields, so a client can request the complete detail in a single round trip.
Inputs `TagInput` and `ConnectionInput` mirror their output types. `ConnectionInput.bidirectional` is non-nullable on input so callers must decide whether the new edge should be traversed both ways.

=== REST

`POST /dsl` accepts `{ "command": "<`#voc("dsl", text: "dsl")` command>" }` and returns `{ "type": ResponseType, "responseObject": [String] }`.
`ResponseType` is one of `FILENAMES`, `CONNECTIONS`, `FILE`, `TAG`, `SUCCESS`, or `ERROR`. The payload shape depends on the type:

- `FILENAMES`: absolute paths.
- `CONNECTIONS`: #voc("json")-serialized `Connection` records.
- `FILE` / `TAG`: #voc("json")-serialized `File` / `TagForFile` records.
- `SUCCESS`: empty list, used by mutating commands such as `addFile`, `addRel`, `addTag`.
- `ERROR`: a single human-readable message.

`GET /download?path=<absolute path>` streams the file contents with a detected `Content-Type`, served by `FileDownloadController`.

=== WebSocket

`/ws` carries the autocomplete protocol used by the GUI.
The client opens a single long-lived session, which can reconnect with exponential back-off if it drops, and sends the current #voc("dsl") prefix on every keystroke.
The server answers with a #voc("json") array of suggestions produced by `DSLAutoCompleter`.
Keeping the connection persistent avoids a TCP handshake per keystroke, which is what makes the 250 ms budget in Q2.1 achievable.

=== Authentication

No authentication is performed on any of the three interfaces.
GraphleManager is intended to run on the operator's own machine or on a remote host reached through SSH port forwarding, as described by the remote-access requirement (Q1.3).
In that model, the forwarded port must remain under the operator's control, so the current version must not be exposed directly to untrusted callers.

Application-level authentication is therefore future work.
A complete remote-access security model would need a uniform layer across #voc("graphql"), #voc("rest"), and the #voc("websocket") handshake, plus authorization checks that map authenticated users to permitted filesystem paths.
This planned extension is deliberately out of scope for the current bachelor's thesis implementation.
