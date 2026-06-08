#import "../../template/shared.typ": *

== Used Technologies

The selection of technologies was guided by three criteria which apply to all components:
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
rather than reimplementing general purpose utilities. Kotlin's type system is also more expressive than Java's. 
Illegal states can be made unrepresentable at compile time more easily, which makes client-server communication less 
error prone by enforcing a more precise contract.

=== API

The application is divided into a backend service and two types of clients: a GUI client and a #link(label("voc_dsl"))[DSL] client.
Both clients communicate exclusively through the #link(label("voc_api"))[API], which allows either to be swapped out or extended by third-party developers,
and enables the backend to serve remote #link(label("voc_filesystem"))[filesystems] over a network without architectural changes.

The backend is implemented with Spring Boot @springboot and exposes two interfaces.
The GUI client communicates via #link(label("voc_graphql"))[GraphQL] @graphql, which allows it to request exactly the data it needs.
This is important since a query result can contain millions of files, and fetching all fields when only a filename is needed would be wasteful.
GraphQL also allows the response structure to evolve without introducing a new #link(label("voc_api"))[API] version, and provides a schema that gives clients full transparency over the available data and operations.
The #link(label("voc_dsl"))[DSL] client communicates via #link(label("voc_rest"))[REST], since the #link(label("voc_dsl"))[DSL] is also intended to be used from the command line, where issuing #link(label("voc_rest"))[REST] calls is simpler than constructing #link(label("voc_graphql"))[GraphQL] queries.

Spring Boot provides a mature ecosystem for building production-grade JVM services, including first-class #link(label("voc_graphql"))[GraphQL] and #link(label("voc_rest"))[REST] support.
Its widespread adoption ensures that contributors with prior JVM experience can onboard with minimal friction.

=== GUI

The graphical client is built with Compose Multiplatform @composemultiplatform, the dominant GUI framework for Kotlin.
Beyond being the most widely adopted option, which eases onboarding for new contributors, Compose Multiplatform
supports sharing UI code across desktop, web, and mobile targets from a single codebase. This makes a future port
to other platforms a low-cost extension rather than a full rewrite.

=== DSL

The application provides a #link(label("voc_dsl"))[domain-specific language (DSL)] for advanced #link(label("voc_filesystem"))[filesystem] operations.
Because autocomplete for the #link(label("voc_dsl"))[DSL] must react to each keystroke, its parser runs inside the backend service and communicates available completions via #link(label("voc_websocket"))[WebSockets]. 
#link(label("voc_websocket"))[WebSockets] maintain a persistent #link(label("voc_connection"))[connection], avoiding the overhead of establishing a new #link(label("voc_http"))[HTTP] connection on every keystroke,
which is critical for meeting the low-latency autocomplete requirement.
gRPC was considered as an alternative, but its additional infrastructure, protobuf schema definitions and code generation,
is overkill for a single stream exchanging short strings.
There are specific tools for creating #link(label("voc_dsl"))[DSLs]. However, as the auto-completer needs to parse the commands either way, and
needs to fetch data from the database, the parser and completer were kept in the same service to avoid serialization between
different services, which is important to keep the response time at minimum.

=== Relationships Between Files

Storing and querying arbitrary #link(label("voc_relationship"))[relationships] between files requires a database whose native data model is a graph.
Two graph data models were considered: the #link(label("voc_lpg"))[Labeled Property Graph (LPG)] and the Resource Description Framework (RDF).

RDF was eliminated for two reasons.
First, it mandates the use of URIs as node and #link(label("voc_relationship"))[relationship] identifiers.
This imposes an unnecessary burden on users who manage a purely local #link(label("voc_filesystem"))[filesystem] with no intent of publishing or interlinking it with external datasets.
Second, RDF stores are typically triple stores that materialise graph structure only at query time, whereas #link(label("voc_lpg"))[LPG] databases index edges natively, resulting in faster traversal for the #link(label("voc_relationship"))[relationship]-heavy workloads this project targets @neo4jrdfvslpg.

Among self-hosted #link(label("voc_lpg"))[LPG] databases, Neo4j @neo4j is the only option with broad industry adoption @so2024survey.
It was therefore selected as the graph store.

=== Autocomplete

As specified in requirement Q2.1, autocomplete responses must be delivered within the threshold of human reaction time, which is approximately 250 ms @humanbenchmark.
Meeting this constraint requires that candidate filenames be held in memory rather than retrieved from disk on each keystroke.

Valkey @valkey, a Linux Foundation fork of Redis @redis, was selected as the in-memory store.
Valkey is reported to be faster and more memory-efficient than Redis in benchmarks @valkeybenchmark, 
and has received corporate backing from Oracle, AWS, and Google. Organisations that now depend on it in production,
 which reduces the risk of the project being abandoned @valkeyannouncement.
Developers familiar with Redis can apply that knowledge directly to Valkey with no additional learning overhead.

To support infix completion while keeping memory consumption reasonable, filenames are indexed using a modified #link(label("voc_trie"))[trie].
Leaf level path components are stored without their parent segments, enabling efficient prefix search across all components of a filename.
Parent path information is preserved as a back reference attached to leaf nodes,
so a full path can be reconstructed from any matching suffix without duplicating the entire path at every entry.
