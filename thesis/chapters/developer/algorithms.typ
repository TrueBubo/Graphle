#import "../../template/shared.typ": *
== Algorithms employed

=== Autocomplete

Autocomplete is implemented as a #voc("trie") storing paths. The #voc("trie") is stored in Valkey, with an in-process #voc("cache") in front of it to save on round trips to the database.
The implementation lives in `dsl/FilenameCompleter.kt` and utilizes a `Storage` abstraction so the #voc("trie") logic can be tested against a fake or swapped for a different storage backend.

Each #voc("trie") node is identified by a numeric index, where the root is assigned index 0.
Each node stores its parent link back up the #voc("trie"), the character it represents, a set of back-references to full-path nodes (used for the reverse-lookup described below), and a flag indicating whether it terminates a complete path.
A separate top-level counter holds the index of the most recently allocated node so inserts pick a fresh index even across process restarts.

The insertion path implements the "modified #voc("trie")" described in the architecture chapter.
Because many files share long common ancestor paths, storing each full path as an independent sequence of characters would duplicate most nodes. The modified #voc("trie") avoids this by inserting each path twice: once as the complete absolute path, and once as only its leaf component, with a back-reference pointing from the leaf to the corresponding full-path node.
The effect is that a directory prefix shared by many files is stored once and reused rather than repeated for every full path. The parent chain for any leaf can always be reconstructed by following the back-reference to the full-path node and then walking up to the root.
Memory therefore grows with the number of unique directory and filename components in the dataset, rather than with the number of files multiplied by their average depth. This matters because the in-process #voc("cache") stores #voc("trie") nodes directly. Duplicated prefixes would fill the #voc("cache") with redundant entries and reduce its effectiveness.

Lookup descends the #voc("trie") character by character from the root, returning an empty result as soon as a character has no child.
From the node reached, `filenameDFS` finds full paths by interleaving three sources: the prefix node itself if it is marked `:full`, the `:parents` set, which contains predecessor path indices, and a recursive descent through the children.
Every returned path is checked for existence on disk, so completions pointing at files deleted since the last sweeper run are filtered out before the suggestions are returned.

Five dedicated #voc("cache", text: "caches"), one per access pattern, sit in front of the storage layer.
Each #voc("cache") entry is refreshed on every access (whether a hit or a miss), so hot nodes stay warm without a separate TTL refresh step. The result is that the first read after startup and all subsequent reads produce the same in-memory view of the data.

To prevent node identifiers from being reused, the database maintains a monotonic `last` counter and assigns each newly created #voc("trie") node the next available key.
This gives every node a stable identity for its entire lifetime, including across process restarts, which in turn makes the application's in-process #voc("cache") safer
as a cached key can be trusted to refer to the same logical node rather than accidentally pointing to newly inserted #voc("trie") state after reuse.

=== Garbage Collector

The sweeper lives in `sweeper/Neo4JSweeper.kt` and keeps the graph consistent with the underlying #voc("filesystem") (F9).
It runs as a single background task that wakes at a configurable interval and executes `sweep`.

A sweep pass proceeds in two phases.
The first phase removes file entries whose recorded locations no longer exist on disk.
The second phase removes #voc("tag") entries that became orphaned as a result of those file deletions.

== DSL

The #voc("dsl") interpreter is organized as a staged pipeline centered around `DSLInterpreter.interpret`.
At the outermost level, the first token identifies the command being invoked.
Commands such as file, #voc("tag"), and #voc("relationship") updates are parsed into typed inputs and then dispatched to the corresponding application service.
The `find` command is structurally richer: rather than mapping directly to a single service operation, it is first parsed, then translated into #voc("cypher"), and only afterwards executed against the graph.
Keeping this query processing pipeline separate from the dispatch path used by simpler commands makes the language easier to extend. Adding a new command such as a file or tag operation usually requires only a new typed input and a new service branch, while changes to the `find` language remain confined to the parser, query builder, and executor.
This reduces coupling between unrelated parts of the #voc("dsl") and lowers the risk that extending one command family will unintentionally affect another.

Conceptually, a `find` query is an ordered sequence of scopes.
Parenthesized scopes describe constraints on file nodes, while bracketed scopes describe constraints on #voc("relationship", text: "relationships"), and the two kinds of scope may be interleaved arbitrarily.
This allows a query to express graph traversals incrementally, alternating between conditions on files and conditions on the #voc("relationship", text: "relationships") that connect them.
The parser's task is to reconstruct this scoped structure from the input, after which each recovered scope is interpreted as a boolean combination of simple predicates.

Once the query has been decomposed into scopes, `CypherQueryBuilder` translates the #voc("dsl") representation into #voc("cypher").
At this stage, user-facing names and operators are mapped onto the corresponding graph properties and comparison operators, and small mismatches between user syntax and storage format are normalized.
For example, numeric comparisons over #voc("tag") values are coerced into numeric form even though the underlying values are stored as strings.
The result is a query that preserves the user's intent while remaining compatible with the graph schema.

Execution is handled by `DSLCommandExecutor`, which submits the generated #voc("cypher") to Neo4j and converts the returned rows into a `DSLResponse`. The shape of the response is determined by the final scope of the query.
If the query ends in a #voc("relationship") scope, the caller receives #voc("relationship", text: "relationships"). If it ends in a file scope, the caller receives filenames. This lets a single surface syntax support several related query patterns without requiring separate command families for each return type.

Autocomplete follows the same structural view of the language. Rather than treating the input buffer as an unstructured string, `DSLAutoCompleter` reconstructs the parse state of the partially written command and infers what kind of token is valid at the cursor position.
Filename completion is therefore invoked only where a filename is syntactically expected.
