#import "../../template/shared.typ": *
== Discussion of implementation problems

=== Real-time autocomplete

Requirement Q2.1 caps an autocomplete response at 250 ms, which rules out doing per-keystroke network work that scales with the input.
A fresh TCP handshake on every keystroke is avoided by keeping a single WebSocket open on `/ws`. The client transparently reconnects with exponential back-off when the link drops, so an established connection is already in place whenever the user is typing.
A Valkey round trip per trie node traversed would in turn make lookup cost linear in the prefix length, which is avoided by fronting each access pattern with an in-process `ConcurrentCache` and by the modified trie encoding described in the Algorithms section.

The 250 ms target is therefore a guarantee that holds once the caches are hot. Under normal interactive use the relevant trie nodes already sit in cache, so a lookup is bounded by local work plus a single network round trip.
It is explicitly *not* a worst-case guarantee for a freshly started session.
A user who logs in after a long absence can see the first few keystrokes exceed the budget while the in-process layer is repopulated from Valkey.

=== Transparent computing

Transparent computing in Graphle means a GUI on one machine can drive a filesystem that lives on another (Q1.3).
The rule that holds this together is simple, all filesystem I/O happens on the server.
The GUI never assumes a path it received from the backend is a local path.
Paths the user sees are just strings that travelled back over GraphQL or REST, and they are only meaningful on the machine where `GraphleManager` runs.

Opening a remote file therefore takes a detour.
The GUI calls `GET /download?path=...` against `FileDownloadController`, writes the response into a `GraphleDownloads/` directory under the user's home, and hands that local copy to the operating system.
When the backend happens to be running on the same host, a `server.localhost` flag in the YAML config skips the download step and opens the file in place.

=== Portability

The application targets macOS and Linux (F10). Windows is deliberately not supported. See the analysis chapter for the reasoning of not supporting it.
Because both the Spring Boot backend and the Compose Desktop frontend are written in Kotlin and compile to JVM bytecode, JDK 21 is the single runtime that makes both components portable. JDK 21 is supported on both MacOS and Linux.
Everything else (file existence, directory enumeration, path normalisation) delegates to `java.nio.file`, which abstracts the remaining OS differences.

=== User navigation

The file-detail view is intentionally the central point of the GUI.
`FileController.fileByLocation` assembles it in a single round trip by pulling from three independent sources: hierarchical neighbours (`descendantsOfFile`, `parentOfFile`) come from the live filesystem via `FileService`, persisted graph relationships come from `ConnectionController.neighborsByFileLocation`, and tags come from `TagController.tagsByFileLocation`.
Before the merged result is returned, every entry is checked with `Files.exists` (and optionally `Files.isHidden`), so a file deleted between the last sweeper run and the current request is silently dropped rather than surfaced to the user.

A subtle consequence is that files which exist on disk but were never explicitly registered with Graphle are still reachable — navigating into a directory shows them under the hierarchical descendants even though they have no `File` node yet (F1).
When the user acts on one of those entries, the relevant mutation creates the node as a side effect.

=== Deployment

GraphleManager depends on two external services, Neo4j and Valkey, each with its own lifecycle.
`compose.yaml` in the backend directory describes both containers with named volumes (`neo4j_data`, `valkey_data`) so `docker compose up -d` is sufficient to bring up a working development environment; the Gradle `bootRun` task then starts the JVM process itself.
The GUI is a separate Gradle project that is either run directly with `./gradlew run` during development or built into a platform-native installer via the Compose Desktop plugin for distribution.

The two services can fail independently, so a Valkey outage while Neo4j remains healthy is an expected operational state.
GraphleManager treats it as degraded rather than fatal. Autocomplete stops working, but every other request continues normally. (Q3.2)
