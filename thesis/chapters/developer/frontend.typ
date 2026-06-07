#import "../../template/shared.typ": *
== Frontend

The frontend is `GraphleUI`, a desktop application built with Compose Multiplatform in Kotlin.
It targets the JVM and is packaged for supported platforms.
Backend access is split between two transports: Apollo #link(label("voc_graphql"))[GraphQL] for all file, tag, and connection operations, and Ktor for the #link(label("voc_dsl"))[DSL] REST call and for the autocomplete WebSocket.

=== Package Layout

All source lives under `com.graphle` and is split by feature. `Main.kt` opens a single Compose `Window` around the root `App` composable, which delegates body rendering to a small dispatcher keyed on the current display mode. The feature packages are:

- `common/`: configuration, singletons, the GraphQL wrapper, a trash helper, the UI state models, and the shared theme and reusable visual primitives.
- `file/`, `tag/`, `fileWithTag/`: one package per main entity, each holding its data model alongside the composables that render it. The file package also collects the fetching, opening, and downloading helpers it needs.
- `dialogs/`: the modal flows for create/delete/move/tag/relationship actions and for surfacing error and invalid-file messages, plus a composite that mounts them inside `App`.
- `header/`: the search header (command line, theme toggle, menu) and the REST and WebSocket transports it drives.
- `dsl/`: command history and the response dispatcher that turns a DSL reply into a display mode change.

=== Configuration

The GUI reads a YAML configuration file with a single `server` section that exposes two options:

- `port`: the TCP port on which the backend is listening. Used to build the GraphQL, REST, download, and WebSocket URLs that the transports target.
- `localhost`: a boolean indicating whether the backend runs on the same host as the GUI. When true, the file opening flow skips the HTTP download step and opens the file in place. When false, the GUI always pulls a local copy through `/download` first.

=== State Management

The UI is driven by a single source of truth held in `App.kt`: `displayedSettings: MutableState<DisplayedSettings>`.
`DisplayedSettings` wraps a nullable `DisplayedData` (the currently visible entities: filenames, tags, connections, or a file detail) and a `DisplayMode` enum that selects which body to render.
`DisplayedBody.kt` branches on the mode and delegates to the feature specific page type.
Child composables never hold the state themselves. `App` hands them paired getter and setter lambdas (`getDisplayedSettings`/`setDisplayedSettings`, and analogous ones for the theme), so the source of truth stays unique and any update, whether from a dialog, a menu entry, or a body composable, flows back through the same write path.

=== Backend Communication

Apollo generates a typed Kotlin client from the server's `schema.graphqls`.
`common/GraphQLCommands.kt` exposes generated operations and maps the Apollo DTOs onto the UI-local models.

`DSLRestManager` posts DSL commands to `POST /dsl` on the configured backend and deserialises the `DSLResponse`.
`DSLCommandHandler` dispatches on `DSLResponse.type`, chooses the matching `DisplayMode`, and calls an `onEvaluation` callback to update the UI state.
`DSLHistory` keeps the last successfully displayed command so the UI can re-submit it after a transient failure (Q3.1).

Real-time autocomplete uses its own connection.
`DSLWebSocketManager` keeps a single session against `/ws`, exposes a flow of incoming suggestions, sends outgoing messages, and reconnects with exponential back-off. This keeps the socket hot across keystrokes so latency is dominated by server-side lookup rather than TCP handshake (Q2.1).
