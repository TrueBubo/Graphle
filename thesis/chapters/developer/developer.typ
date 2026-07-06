#import "../../template/shared.typ": *
= Developer Documentation <developer>

This chapter describes the implementation of Graphle for a developer who wants to work with the source code.
It complements the design chapter, which explains why the system is structured the way it is, by focusing on how that structure is implemented.
The codebase consists of two applications: `GraphleManager`, a backend written in Kotlin and Spring Boot, and `GraphleUI`, a desktop client written in Compose Multiplatform.
These applications can be developed independently, and the only contract the developers need to adhere to is the #voc("api") communication defined in a subsequent section.

The chapter begins with the package layout of both applications and the concrete classes, repositories, and services that make up each layer. It is followed by a sequence-diagram walkthrough of the most representative end-to-end flows.
It then outlines all #voc("api") endpoints in detail, providing paths, methods, parameters, and example payloads across the #voc("graphql"), #voc("rest"), and #voc("websocket") interfaces.
Next, it describes the algorithms behind the core features, the modified #voc("trie") used for autocomplete, the background sweeper that keeps the graph consistent with the #voc("filesystem"), and the multi-stage #voc("dsl") interpreter pipeline. It also discusses the problems that arose during implementation.
Finally, the chapter concludes with the testing strategy and the continuous-integration setup that ensures the codebase keeps working as future contributors extend it.

The implementation follows the design chapter without major deviation, and the requirements defined in the analysis are fulfilled by the resulting program, as summarised in @req-traceability.

#figure(
  caption: [Requirements traceability: each requirement mapped to its design component, key implementation class or module, and automated test.],
  placement: none,
  kind: table,
  {
    set text(size: 8pt)
    table(
      columns: (auto, 1.4fr, 2.2fr, 2fr),
      stroke: 0.4pt + luma(160),
      inset: 4pt,
      align: left,
      table.header([*Req.*], [*Design component*], [*Implementation*], [*Test*]),
      [F1],   [File module],                                                   [`FileController`, `FileService`, `FileRepository`],                                                                                                [`FileIntegrationTest`],
      [F2],   [Connection module],                                             [`ConnectionController`, `ConnectionService`, `ConnectionRepository`],                                                                              [`ConnectionIntegrationTests`],
      [F3],   [Tag module],                                                    [`TagController`, `TagService`, `TagRepository`],                                                                                                    [`TagIntegrationTests`],
      [F4],   [File module],                                                   [`FileController`, `FileService`, `FileRepository`],                                                                                                 [`FileIntegrationTest`],
      [F5],   [File module],                                                   [`FileService`, `FileController`],                                                                                                                   [`FileIntegrationTest`],
      [F6],   [GraphleUI],                                                     [`GraphleUI` project],                                                                                                                               [Manual],
      [F7],   [DSL module],                                                    [`DSLController`, `DSLInterpreter`, `CypherQueryBuilder`, `DSLCommandExecutor`],                                                                    [`DSLInterpreterTest`, `DSLInterpreterIntegrationTest`],
      [F8],   [Autocomplete module],                                           [`FilenameCompleter`, `DSLAutoCompleter`, `DSLWebSocketManager`],                                                                                    [`ValkeyFilenameCompleterTest`],
      [F9],   [File module],                                                   [`FileService`, `Neo4JSweeper`],                                                                                                                     [`Neo4JSweeperTest`],
      [F10],  [Chosen stack, File module],                                     [`FileService`],                                                                                                                                     [Manual],
      [Q1.1], [GraphleUI containers + DSL module],                             [`FileController`, `TagController`, `ConnectionController`, `DSLController`],                                                                       [`FileIntegrationTest`, `TagIntegrationTests`, `ConnectionIntegrationTests`, `DSLInterpreterIntegrationTest`],
      [Q1.2], [Public API, GraphleUI theme],                                   [`GraphQLCommands`, GraphleUI `common/`],                                                                                                            [Manual],
      [Q1.3], [Boundary between GraphleManager and GraphleUI, download endpoint], [`FileDownloadController`, `DSLRestManager`],                                                                                                        [`FileDownloadControllerTest`],
      [Q2.1], [Autocomplete module],                                           [`FilenameCompleter`, `DSLWebSocketManager`],                                                                                                        [#link(label("autocomplete-latency"))[Latency measurement]],
      [Q3.1], [WebSocket transport],                                           [`DSLWebSocketManager`],                                                                                                                             [Manual],
      [Q3.2], [Module boundaries (@graphle-manager-components-c4)],            [`FileService`, `TagService`, `ConnectionService`],                                                                                                  [`FileIntegrationTest`, `TagIntegrationTests`, `ConnectionIntegrationTests`],
      [Q4.1], [DSL module, parser/dispatch split],                             [`DSLInterpreter`, DSL parser],                                                                                                                      [`DSLInterpreterTest`],
    )
  }
) <req-traceability>

#include "backend.typ"

#include "frontend.typ"

#include "sequence.typ"

#include "api.typ"

#include "algorithms.typ"

#include "problem-discussion.typ"

#include "testing.typ"
