#import "../../template/shared.typ": *
#import "sequence/add-file-with-tag.typ": add-file-with-tag-diagram
#import "sequence/dsl-query.typ": dsl-query-diagram
#import "sequence/autocomplete-keystroke.typ": autocomplete-keystroke-diagram
#import "sequence/sweeper.typ": sweeper-diagram

== Sequence Diagrams

This section walks through four representative end-to-end flows.
The first two are the most common write and read paths a user issues from the GUI and the #voc("dsl") client. The third describes how the autocomplete loop meets the 250 ms latency budget (Q2.1). The fourth shows the background work that keeps the database consistent with the #voc("filesystem") (F9).
In every diagram, solid arrows represent synchronous calls and dashed arrows represent returned values. The `↺` glyph marks work performed on the actor's own lifeline (an asynchronous task, a disk write, or an internal computation).

=== Adding a File with a Tag

The "add file" dialog is triggered from the GUI and a #voc("tag") is attached to the newly created file.
The first round trip creates the file on disk and the `File` node in Neo4j, and, as a side effect, asks the autocomplete service to index the new path.
The second round trip merges the #voc("tag") node and the `HasTag` edge.
After both complete, the GUI re-fetches the file detail to display the updated tag set.

#add-file-with-tag-diagram()

=== DSL `find` Query

The #voc("dsl") client posts a command string to `/dsl`.
`DSLInterpreter` selects the `find` branch, and the query is split into file- and #voc("relationship")-level scopes by `DSLScopeParser`, compiled into #voc("cypher") by `CypherQueryBuilder`, and executed by `DSLCommandExecutor` against Neo4j.
The response type (`FILENAMES` vs `CONNECTIONS`) is derived from the last scope of the query so that the client can choose a matching `DisplayMode` without a second round trip.

#dsl-query-diagram()

=== Autocomplete Keystroke

Every keystroke in the #voc("dsl") command line sends the current prefix over a persistent #voc("websocket").
`DSLAutoCompleter` first classifies the command kind from the opening token, then asks `FilenameCompleter` for up to five completions.
The filename completer reads its #voc("trie") primarily from an in-process `ConcurrentCache` and falls back to Valkey only on a miss. Every Valkey access also refreshes the key's TTL so hot entries stay warm for `cache.ttl`.

#autocomplete-keystroke-diagram()

=== Background Sweeper

`Neo4JSweeper` runs in its own coroutine and wakes up every `neo4j.sweeper.interval`.
It lists all `File` nodes, tests each for #voc("filesystem") existence through an injectable predicate, removes the stale ones with `DETACH DELETE`, and then prunes any #voc("tag") nodes that lost their last incident edge.
The sweeper is the only component that reconciles the database with external #voc("filesystem") changes, which is what allows the rest of the system to treat Neo4j as eventually consistent with the disk.

#sweeper-diagram()
