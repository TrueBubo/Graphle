#import "../../template/shared.typ": *

== Used Technologies

The selection of technologies was guided by three criteria that apply to all components:
cross-platform compatibility to ensure the application runs on macOS and Linux without platform-specific defects (requirement F10),
ecosystem maturity and community size to lower the barrier for future contributors and reduce the risk of relying on abandoned dependencies,
and fit for the specific technical constraints of each component, such as latency requirements or data-model alignment.
Where multiple candidates satisfied the first two criteria, the decision was made on technical fit.

=== Programming Language

The project targets macOS and Linux (requirement F10), so the chosen language must be fully cross-platform.
This ensures users do not encounter configuration-specific bugs, reducing the risk of failures that would go undetected during routine developer testing on a single platform.

The language must also be suitable for building large-scale desktop GUI applications and have a sufficiently large community.
Since other developers should be able to extend the project with new features, a niche language without an established ecosystem is not suitable.
At the time of project inception, the only candidates meeting both constraints were Java @java and Kotlin @kotlin.

Kotlin was chosen over Java because it supports every Java library while also offering additional language features.
This gives developers access to a vast span of existing functionality, allowing them to focus on the core domain 
rather than reimplementing general-purpose utilities. Kotlin's type system is also more expressive than Java's.
Illegal states can be made unrepresentable at compile time more easily, which makes client-server communication less 
error-prone by enforcing a more precise contract.

=== API

The application is divided into a backend service and two types of clients: a GUI client and a #voc("dsl") client.
Both clients communicate exclusively through the #voc("api"), which allows either to be swapped out or extended by third-party developers,
and enables the backend to serve remote #voc("filesystem", text: "filesystems") over a network without architectural changes.

The backend is implemented with Spring Boot @springboot and exposes three interfaces.
The GUI client communicates via #voc("graphql") @graphql, which allows it to request exactly the data it needs.
This is important since a query result can contain millions of files, and fetching all fields when only a filename is needed would be wasteful.
The #voc("dsl") client communicates via #voc("rest"), since the #voc("dsl") is also intended to be used from the command line, where issuing #voc("rest") calls is simpler than constructing #voc("graphql") queries.
Lastly, the backend also provides a WebSocket endpoint used by the GUI to deliver low-latency autocomplete suggestions while the user types DSL commands.

Spring Boot provides a mature ecosystem for building production-grade JVM services, including first-class #voc("graphql") and #voc("rest") support.
Its widespread adoption ensures that contributors with prior JVM experience can onboard with minimal friction.

=== GUI

The graphical client is built with Compose Multiplatform @composemultiplatform, a major GUI framework for Kotlin.
Beyond being a widely adopted option, which eases onboarding for new contributors, Compose Multiplatform
supports sharing UI code across desktop, web, and mobile targets from a single codebase. This makes a future port
to other platforms a low-cost extension rather than a full rewrite.

=== DSL

The application provides a #voc("dsl") for advanced #voc("filesystem") operations.
Because autocomplete for the #voc("dsl") must react to each keystroke, its parser runs inside the backend service and communicates available completions via #voc("websocket", text: "WebSockets"). 
#voc("websocket", text: "WebSockets") maintain a persistent #voc("connection"), avoiding the overhead of establishing a new #voc("http") connection on every keystroke,
which is critical for meeting the low-latency autocomplete requirement.
gRPC was considered as an alternative, but its additional infrastructure, protobuf schema definitions, and code generation,
is overkill for a single stream exchanging short strings.
There are specific tools for creating #voc("dsl", text: "DSLs"). However, as the auto-completer needs to parse the commands either way and
needs to fetch data from the database, the parser and completer were kept in the same service to avoid serialization between
different services, which is important to keep the response time to a minimum.

=== Relationships Between Files

Storing and querying arbitrary #voc("relationship", text: "relationships") between files requires a database whose native data model is a graph.
Two graph data models were considered: the #voc("lpg") and the Resource Description Framework (RDF).

RDF was eliminated for two reasons.
First, it mandates the use of URIs as node and #voc("relationship") identifiers.
This imposes an unnecessary burden on users who manage a purely local #voc("filesystem") with no intent of publishing or interlinking it with external datasets.
Second, RDF stores are typically triple stores that materialize graph structure only at query time, whereas #voc("lpg") databases index edges natively, resulting in faster traversal for the #voc("relationship")-heavy workloads this project targets @neo4jrdfvslpg.

After selecting the #voc("lpg") model, several self-hosted databases capable of storing
property graphs were compared: ArangoDB, ArcadeDB, and Neo4j. ArangoDB @arangodb and
ArcadeDB @arcadedb are multi-model databases that combine graph storage with document
and other data models, which would be useful if Graphle stored several kinds of data in
one database. Graphle only persists semantic graph metadata, so a dedicated graph
database that stores the data directly as a graph is a better fit. Furthermore, Neo4j @neo4j was
selected because it is a mature self-hosted #voc("lpg") database with the broadest adoption
@so2024survey, #voc("cypher") support, and direct Spring integration
through Spring Data Neo4j @springdataneo4j.

=== Autocomplete

As specified in requirement Q2.1, autocomplete responses must be delivered within the threshold of human reaction time, which is approximately 250 ms @humanbenchmark.
Meeting this constraint requires that candidate filenames be held in memory rather than retrieved from disk on each keystroke.

Valkey @valkey, a Linux Foundation fork of Redis @redis, was selected as the in-memory store.
Valkey is reported to be faster and more memory-efficient than Redis in benchmarks @valkeybenchmark
and has received corporate backing from Oracle, AWS, and Google. Organizations now depend on it in production,
which reduces the risk of the project being abandoned @valkeyannouncement.
Developers familiar with Redis can apply that knowledge directly to Valkey with no additional learning overhead.

To support infix completion while keeping memory consumption reasonable, filenames are indexed using a modified #voc("trie").
Leaf level path components are stored without their parent segments, enabling efficient prefix search across all components of a filename.
Parent path information is preserved as a back reference attached to leaf nodes,
so a full path can be reconstructed from any matching suffix without duplicating the entire path at every entry.
