#import "../../template/shared.typ": *

== Module Decomposition

This section adds a C4 view of the system.
The diagrams define the architectural boundaries that follow from the requirements and complement the component-level design.
The module names follow the current package structure of `GraphleManager` and `GraphleUI`, so the diagrams can be traced back to the implementation.

=== System Context

At the highest level, Graphle is a desktop-oriented file management system that extends the user's existing #link(label("voc_filesystem"))[filesystem] with graph #link(label("voc_metadata"))[metadata].
The operating #link(label("voc_filesystem"))[filesystem] remains the source of truth for file existence, hierarchy, and file contents.
Graphle stores only semantic #link(label("voc_metadata"))[metadata] in Neo4j and data used for autocomplete in Valkey.

#figure(
  placement: none,
  image("architecture/graphle-context-c4.svg", width: 100%),
  caption: [C4 system context diagram]
) <graphle-context-c4>

The @graphle-context-c4 separates Graphle from the external systems it depends on.
This separation is important for backward compatibility (F1), because files can still be manipulated by normal operating-system tools.
It is also important for component isolation (Q3.2), because the autocomplete datastore can fail without preventing the core graph and filesystem operations from running.

=== Containers

The system is split into a desktop GUI container and a backend container.
`GraphleUI` is a Kotlin Compose Multiplatform application.
It communicates with `GraphleManager` through public network interfaces only: #link(label("voc_graphql"))[GraphQL] for file, #link(label("voc_tag"))[tag], and #link(label("voc_relationship"))[relationship] operations, #link(label("voc_rest"))[REST] for #link(label("voc_dsl"))[DSL] execution and file downloads, and #link(label("voc_websocket"))[WebSocket] for command autocomplete.
`GraphleManager` is a Spring Boot service that coordinates #link(label("voc_filesystem"))[filesystem] operations, the Neo4j graph, and the Valkey-backed autocomplete index.

#figure(
  placement: none,
  image("architecture/graphle-containers-c4.svg", width: 100%),
  caption: [C4 container diagram]
) <graphle-containers-c4>

The @graphle-containers-c4 shows that the GUI is not coupled to the database and filesystem directly.
This supports remote access (Q1.3) and extensibility (Q4.1), as another client can reuse the same public #link(label("voc_api"))[API]
instead of having to rely on the current state of backend internals.

=== GraphleManager Modules

The backend module decomposition follows the source packages in `GraphleManager/src/main/kotlin/com/graphle/graphlemanager`.
The #link(label("voc_api"))[API] adapters accept client traffic and delegate to service modules.
The service modules contain the application logic and decide whether an operation needs Neo4j, the live #link(label("voc_filesystem"))[filesystem], Valkey, or a combination of them.

#figure(
  placement: none,
  image("architecture/graphle-manager-components-c4.svg", width: 100%),
  caption: [C4 component diagram of GraphleManager]
) <graphle-manager-components-c4>

The @graphle-manager-components-c4 highlights the main backend boundaries.
The file module is the only module that directly writes to the #link(label("voc_filesystem"))[filesystem].
The #link(label("voc_tag"))[tag] and #link(label("voc_connection"))[connection] modules are graph-specific and persist through Neo4j repositories.
The #link(label("voc_dsl"))[DSL] module reuses the same domain services for commands that change state, while complex `find` queries are translated to #link(label("voc_cypher"))[Cypher] and executed against Neo4j.
The autocomplete module owns the Valkey-backed #link(label("voc_trie"))[trie] and is updated asynchronously from file navigation.

=== GraphleUI Modules

The frontend module decomposition follows the packages in `GraphleUI/src/main/kotlin/com/graphle`.
`App.kt` owns the displayed state and delegates rendering to the header, dialog, and body modules.
All backend communication is kept in transport helpers, which lets the composable UI code work with local models instead of raw #link(label("voc_graphql"))[GraphQL] or #link(label("voc_http"))[HTTP] payloads.

#figure(
  placement: none,
  image("architecture/graphle-ui-components-c4.svg", width: 100%),
  caption: [C4 component diagram of GraphleUI]
) <graphle-ui-components-c4>

The @graphle-ui-components-c4 shows how the GUI keeps its visible content in one shared state model.
Actions initiated from the command line, dialogs, or file views are sent to the backend through the appropriate transport.
The returned data is converted to `DisplayedSettings`, which `App` then uses to select and update the currently displayed body.
As a result, graphical workflows and #link(label("voc_dsl"))[DSL]-driven workflows update the interface through the same state path.
