#import "../../template/shared.typ": *
== Algorithms employed

=== Autocomplete

Autocomplete is implemented as a trie storing paths. The trie is stored in Valkey, with an in-process cache in front of it to save on trips going to the database.
The implementation lives in `dsl/FilenameCompleter.kt` and utilazes a `Storage` abstraction so the trie logic can be tested against a fake or utilize a whole different database system.

Each trie node is identified by a numeric index, where the root key is 0.
The children of a node are stored as a Valkey hash at the node's key, mapping one character to the index of the child node.
Four auxiliary keys hang off every node, `{key}:prev` (tree parent in the trie), `{key}:val` (the character at this node), `{key}:parents` (a set of predecessor path indices used for the reverse-lookup described below), and `{key}:full` (indicates whether the node terminates a complete path).
A separate top-level `last` key holds the index of the most recently allocated node so inserts pick a fresh index even across process restarts.

The insertion path is the interesting part of the design, and implements the "modified trie" described in the architecture chapter.
Insert joins the full absolute path with a file separator and calls `insertComponent` once for the whole path (marking its terminal node with `:full`), then a second time for the leaf component alone — but linking it back to the full-path node through the `:parents` set.
The effect is that long paths with a shared ancestry are not duplicated at every level of the trie. The parent chain for any leaf can always be reconstructed by walking `:parents` to a full-path node and then following `:prev` back to the root.
In practical terms, a directory prefix shared by many files is stored once and reused, instead of being repeated separately for every full path. Memory therefore grows with the number of unique directory and filename components present in the dataset, rather than with the number of files multiplied by their average depth. That matters because the in-process hot-path cache stores trie nodes directly,
if every deep path duplicated the same prefixes, the cache would fill with redundant nodes and lose much of its benefit.

Lookup descends the trie character by character from the root, returning an empty result as soon as a character has no child.
From the node reached, `filenameDFS` finds full paths by interleaving three sources. The prefix node itself if it is marked `:full`, the `:parents` set which contains predecessor path indices, and a recursive descent through the children.
Every returned path is checked whether it still exists, which means completions that point at files the user has since deleted are filtered out even if the sweeper has not yet run.

Five cache instances (`childrenCache`, `previousLevelCache`, `treeParentCache`, `valueCache`, `fullPathEndCache`) sit in front of the `Storage` reads.
Each `Storage` accessor is responsible for keeping its cache entry in sync with the value just read. On a cache miss, it fetches the value from storage and inserts it into the appropriate cache. On a cache hit, it refreshes that same cache entry. The result is that the first read after startup and later repeated reads both leave the process with the same in-memory view of the data.

To prevent node identifiers from being reused, the database maintains a monotonic `last` counter and assigns each newly created trie node the next available key.
This gives every node a stable identity for its entire lifetime, including across process restarts, which in turn makes the application's in-process cache safer
as a cached key can be trusted to refer to the same logical node rather than accidentally pointing to newly inserted trie state after reuse.

=== Garbage Collector

The sweeper lives in `sweeper/Neo4JSweeper.kt` and keeps the graph consistent with the underlying filesystem (F9).
It runs as a single background task that wakes at a configurable interval and executes `sweep`.

A sweep pass proceeds in two phases.
The first phase removes file entries whose recorded locations no longer exist on disk.
The second phase removes tag entries that became orphaned as a result of those file deletions.

== DSL

The DSL interpreter is organized as a staged pipeline centered around `DSLInterpreter.interpret`.
At the outermost level, the first token identifies the command being invoked.
Commands such as file, tag, and relationship updates are parsed into typed inputs and then dispatched to the corresponding application service.
The `find` command is structurally richer: rather than mapping directly to a single service operation, it is first parsed, then translated into Cypher, and only afterwards executed against the graph.
Keeping this query processing pipeline separate from the dispatch path used by simpler commands makes the language easier to extend. Adding a new command such as a file or tag operation usually requires only a new typed input and a new service branch, while changes to the `find` language remain confined to the parser, query builder, and executor.
This reduces coupling between unrelated parts of the DSL and lowers the risk that extending one command family will unintentionally affect another.

Conceptually, a `find` query is an ordered sequence of scopes.
Parenthesized scopes describe constraints on file nodes, while bracketed scopes describe constraints on relationships, and the two kinds of scope may be interleaved arbitrarily.
This allows a query to alternate between conditions on files and conditions on the relationships that connect them,
which gives the language a concise way to express graph traversals incrementally.
The parser's task is to reconstruct this scoped structure from the input, after which each recovered scope is interpreted as a boolean combination of simple predicates.

Once the query has been decomposed into scopes, `CypherQueryBuilder` translates the DSL representation into Cypher.
At this stage, user-facing names and operators are mapped onto the corresponding graph properties and comparison operators, and small mismatches between user syntax and storage format are normalized.
For example, numeric comparisons over tag values are coerced into numeric form even though the underlying values are stored as strings.
The result is a query that preserves the user's intent while remaining compatible with the graph schema.

Execution is handled by `DSLCommandExecutor`, which submits the generated Cypher to Neo4j and converts the returned rows into a `DSLResponse`. The shape of the response is determined by the final scope of the query.
If the query ends in a relationship scope, the caller receives relationships. If it ends in a file scope, the caller receives filenames. This lets a single surface syntax support several related query patterns without requiring separate command families for each return type.

Autocomplete follows the same structural view of the language. Rather than treating the input buffer as an unstructured string, `DSLAutoCompleter` reconstructs the parse state of the partially written command and infers what kind of token is valid at the cursor position.
Filename completion is therefore invoked only where a filename is syntactically expected.