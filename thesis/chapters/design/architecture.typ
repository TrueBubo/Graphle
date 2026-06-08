#import "../../template/shared.typ": *
== Architecture

The system is composed of five components: GraphleManager, GraphleUI, the #link(label("voc_dsl"))[DSL] client, Neo4j, and Valkey.
The application is not purely RESTful.
GraphleManager exposes three distinct communication interfaces - #link(label("voc_graphql"))[GraphQL], #link(label("voc_rest"))[REST], and #link(label("voc_websocket"))[WebSocket] - each chosen for the specific needs of the client that uses it.
The following sections describe how each component and interface addresses the #link(label("functional_requirements"))[functional] and #link(label("qualitative_requirements"))[qualitative] requirements defined in the analysis.

=== Components

*GraphleManager* is the backend service and the single point of contact for all clients.
It hosts the #link(label("voc_dsl"))[DSL] interpreter, manages both external datastores, and exposes the three communication interfaces described below.
Because GraphleManager is a network service, clients can connect to an instance running on a remote machine, giving users access to a remote #link(label("voc_filesystem"))[filesystem] (Q1.3).
It is implemented on the JVM, which together with Compose Multiplatform on the client side ensures the application runs on both macOS and Linux without duplicating the logic (F10).
When an auxiliary component such as Valkey or a connected GUI becomes unavailable, GraphleManager continues to handle all other operations.
Failures in optional subsystems are caught and logged without propagating to the core (Q3.2).

*GraphleUI* is the desktop GUI client built with Compose Multiplatform (F6).
It communicates with GraphleManager exclusively through the public #link(label("voc_api"))[API], which means it can be replaced by any third-party client that speaks the same interfaces (Q1.2).
The default client ships with both a light and a dark theme (Q1.2).
File and folder manipulation is exposed through #link(label("voc_graphql"))[GraphQL] mutations for the GUI and through the #link(label("voc_dsl"))[DSL] for the command line client (F4, F5).

*DSL client* is a command line tool that sends commands to GraphleManager over #link(label("voc_rest"))[REST] (F7).
It is the second user interface available to users alongside the GUI (Q1.1).

*Neo4j* stores the graph of files, #link(label("voc_relationship"))[relationships], and #link(label("voc_tag"))[tags] as a #link(label("voc_lpg"))[Labeled Property Graph] (F2, F3).
Only GraphleManager reads from and writes to Neo4j directly, and the clients access the database layer through it.
However, the database instance is exposed on a configurable port with a published schema. This allows external applications 
and independent forks of GraphleManager to query or even extend the dataset directly, fulfilling the extensibility requirement (Q4.1).

*Valkey* holds the filename autocomplete index in memory as a #link(label("voc_trie"))[trie] structure (F8).
GraphleManager populates and queries this index on behalf of clients.
Frequently accessed #link(label("voc_trie"))[trie] nodes are additionally cached inside GraphleManager using a set of concurrent #link(label("voc_cache"))[caches], so hot nodes are served without a network round trip to Valkey.
If Valkey is unavailable, autocomplete is silently disabled while all other operations continue (Q3.2).

=== Communication Interfaces

*#link(label("voc_graphql"))[GraphQL]* (`/graphql`) is used by GraphleUI for all file, #link(label("voc_tag"))[tag], and #link(label("voc_connection"))[connection] operations.
It allows the client to request exactly the fields it needs, which is important when a query result can contain a large number of files.
The schema also gives any future client full transparency over the available data and operations.

*#link(label("voc_rest"))[REST]* (`/dsl`, `/download`) is used by the #link(label("voc_dsl"))[DSL] client and by GraphleUI for #link(label("voc_dsl"))[DSL] command execution and file downloads.
#link(label("voc_rest"))[REST] was chosen for these endpoints because issuing an #link(label("voc_http"))[HTTP] request from the command line is straightforward, whereas constructing a #link(label("voc_graphql"))[GraphQL] query would add unnecessary overhead for CLI use.

*#link(label("voc_websocket"))[WebSocket]* (`/ws`) provides a persistent connection for #link(label("voc_dsl"))[DSL] autocomplete.
Keeping the connection open avoids establishing a new TCP handshake on each keystroke, which is necessary to meet the latency requirement (Q2.1).
GraphleUI reconnects automatically with exponential back-off if the connection is lost. (Q3.1)

=== Background Maintenance and Lazy Loading

Files, parent directories, and descendants are not indexed eagerly.
They are loaded from the #link(label("voc_filesystem"))[filesystem] on demand the first time a query requests them (F9).
This avoids a potentially unbounded startup scan and keeps the graph populated only with content the user has actually navigated to.
Parent and descendant #link(label("voc_relationship"))[relationships] are resolved live by reading the OS #link(label("voc_filesystem"))[filesystem] hierarchy rather than being stored in Neo4j, 
meaning any file already on disk is immediately accessible through Graphle without first being loaded to database (F1).

A background job, *Neo4JSweeper*, runs inside GraphleManager at a configurable interval.
It fetches all file locations recorded in Neo4j, checks whether each path still exists on the #link(label("voc_filesystem"))[filesystem],
and removes any that have been deleted outside the application along with their orphaned #link(label("voc_tag"))[tags] and #link(label("voc_relationship"))[relationships].
This keeps the database consistent with the underlying #link(label("voc_filesystem"))[filesystem] without requiring the user to manually clean up deleted files.
