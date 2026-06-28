#import "../../template/shared.typ": *

== Module Decomposition

This section adds a C4 view of the system.
The diagrams define the architectural boundaries that follow from the requirements and complement the component-level design.
The module names follow the current Gradle modules and package structure of `GraphleManager` and `GraphleUI`, so the diagrams can be traced back to the implementation.

=== System Context

At the highest level, Graphle is a desktop-oriented file management system that extends the user's existing #voc("filesystem") with graph #voc("metadata").
The operating #voc("filesystem") remains the source of truth for file existence, hierarchy, and file contents.
Graphle stores only semantic #voc("metadata") in Neo4j and data used for autocomplete in Valkey.

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
It communicates with `GraphleManager` through public network interfaces only: #voc("graphql") for file, #voc("tag"), and #voc("relationship") operations, #voc("rest") for #voc("dsl") execution and file downloads, and #voc("websocket") for command autocomplete.
`GraphleManager` is a Spring Boot service that coordinates #voc("filesystem") operations, the Neo4j graph, and the Valkey-backed autocomplete index.

#figure(
  placement: none,
  image("architecture/graphle-containers-c4.svg", width: 100%),
  caption: [C4 container diagram]
) <graphle-containers-c4>

The @graphle-containers-c4 shows that the GUI is not coupled to the database and filesystem directly.
This supports remote access (Q1.3) and extensibility (Q4.1), as another client can reuse the same public #voc("api")
instead of having to rely on the current state of backend internals.

=== GraphleManager Modules

The backend module decomposition follows the Gradle modules in `GraphleManager`.
The #voc("api") adapters accept client traffic and delegate to service modules.
The service modules contain the application logic and decide whether an operation needs Neo4j, the live #voc("filesystem"), Valkey, or a combination of them.

#figure(
  placement: none,
  image("architecture/graphle-manager-components-c4.svg", width: 100%),
  caption: [C4 component diagram of GraphleManager]
) <graphle-manager-components-c4>

The @graphle-manager-components-c4 highlights the main backend boundaries.
The file module is the only module that directly writes to the #voc("filesystem").
The #voc("tag") and #voc("connection") modules are graph-specific and persist through Neo4j repositories.
The #voc("dsl") module reuses the same domain services for commands that change state, while complex `find` queries are translated to #voc("cypher") and executed against Neo4j.
The autocomplete module owns the Valkey-backed #voc("trie") and is updated asynchronously from file navigation.

=== GraphleUI Modules

The frontend module decomposition follows the packages in `GraphleUI/src/main/kotlin/com/graphle`.
`App.kt` owns the displayed state and delegates rendering to the header, dialog, and body modules.
All backend communication is kept in transport helpers, which lets the composable UI code work with local models instead of raw #voc("graphql") or #voc("http") payloads.

#figure(
  placement: none,
  image("architecture/graphle-ui-components-c4.svg", width: 100%),
  caption: [C4 component diagram of GraphleUI]
) <graphle-ui-components-c4>

The @graphle-ui-components-c4 shows how the GUI keeps its visible content in one shared state model.
Actions initiated from the command line, dialogs, or file views are sent to the backend through the appropriate transport.
The returned data is converted to `DisplayedSettings`, which `App` then uses to select and update the currently displayed body.
As a result, graphical workflows and #voc("dsl")-driven workflows update the interface through the same state path.
