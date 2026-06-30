#import "../../template/shared.typ": *
== Discussion of implementation problems

=== Real-time autocomplete

Requirement Q2.1 caps an autocomplete response at 250 ms, which rules out doing per-keystroke network work that scales with the input.
A fresh TCP handshake on every keystroke is avoided by keeping a single #voc("websocket") open on `/ws`. The client transparently reconnects with exponential back-off when the link drops, so an established #voc("connection") is already in place whenever typing occurs.
A Valkey round trip per #voc("trie") node traversed would in turn make lookup cost linear in the prefix length, which is avoided by fronting each access pattern with an in-process #voc("cache") and by the modified #voc("trie") encoding described in the Algorithms section.

The implementation is designed to make the 250 ms target realistic for established interactive sessions with warm #voc("cache", text: "caches").
The manual measurements in the Testing section show that the sampled requests stayed below this threshold.
The first few keystrokes after a cold start may exceed the budget while the cache is repopulated from Valkey, and slower responses are also possible under unusual system load or slow filesystem/database access.

=== Transparent computing

Transparent computing in Graphle means a GUI on one machine can drive a #voc("filesystem") that lives on another (Q1.3).
The invariant that holds this together is that all #voc("filesystem") I/O is performed on the server.
The GUI never assumes a path received from the backend is a local path.
Paths are meaningful only on the host where `GraphleManager` runs.

Opening a remote file therefore takes a detour.
The GUI calls `GET /download?path=...` against `FileDownloadController`, writes the response into a `GraphleDownloads/` directory under the user's home, and hands that local copy to the operating system.
When the backend happens to be running on the same host, a `server.localhost` flag in the YAML config skips the download step and opens the file in place.

=== Portability

The application targets macOS and Linux (F10). Windows is deliberately not supported. See the analysis chapter for the reasoning behind not supporting it.
Because both the Spring Boot backend and the Compose Desktop frontend are written in Kotlin and compile to JVM bytecode, JDK 21 is the single runtime that makes both components portable. JDK 21 is supported on both macOS and Linux.
Everything else (file existence, directory enumeration, path normalization) delegates to `java.nio.file`, which abstracts the remaining OS differences.

=== User navigation

The file-detail view is intentionally the central point of the GUI.
`FileDetailsService.fileByLocation` in the `application` module assembles it in a single round trip by pulling from three independent sources: hierarchical #voc("neighbor", text: "neighbors") (`descendantsOfFile`, `parentOfFile`) come from the live #voc("filesystem") via `FileService`, persisted graph #voc("relationship", text: "relationships") come from `ConnectionService.neighborsByFileLocation`, and #voc("tag", text: "tags") come from `TagService.tagsByFileLocation`.
Before the merged result is returned, every entry is checked with `Files.exists` (and optionally `Files.isHidden`), so a file deleted between the last sweeper run and the current request is silently dropped rather than surfaced to the user.

A subtle consequence is that files which exist on disk but were never explicitly registered with Graphle are still reachable: navigating into a directory shows them under the hierarchical descendants even though they have no `File` node yet (F1).
When the user acts on one of those entries, the relevant mutation creates the node as a side effect.

=== Deployment

GraphleManager depends on two external services, Neo4j and Valkey, each with its own lifecycle.
`compose.yaml` in the backend directory describes both containers with named volumes (`neo4j_data`, `valkey_data`) so `docker compose up -d` is sufficient to bring up a working development environment. The Gradle `bootRun` task then starts the JVM process itself.
The GUI is a separate Gradle project that is either run directly with `./gradlew run` during development or built into a platform-native installer via the Compose Desktop plugin for distribution.

The two services can fail independently, so a Valkey outage while Neo4j remains healthy is an expected operational state.
GraphleManager treats it as degraded rather than fatal. Autocomplete stops working, but every other request continues normally (Q3.2).
