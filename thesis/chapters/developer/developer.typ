#import "../../template/shared.typ": *
= Developer Documentation

This chapter describes the implementation of Graphle for a developer who wants to work with the source code.
It complements the design chapter, which explains why the system is structured the way it is, by focusing on how that structure is implemented.
The codebase consists of two applications: `GraphleManager`, a backend written in Kotlin and Spring Boot, and `GraphleUI`, a desktop client written in Compose Multiplatform.
These applications can be developed independently, and the only contract the developers need to adhere to is the #voc("api") communication defined in a subsequent section.

The chapter begins with the package layout of both applications and the concrete classes, repositories, and services that make up each layer. It is followed by a sequence-diagram walkthrough of the most representative end-to-end flows.
It then outlines all #voc("api") endpoints in detail, providing paths, methods, parameters, and example payloads across the #voc("graphql"), #voc("rest"), and #voc("websocket") interfaces.
Next, it describes the algorithms behind the core features, the modified #voc("trie") used for autocomplete, the background sweeper that keeps the graph consistent with the #voc("filesystem"), and the multi-stage #voc("dsl") interpreter pipeline. It also discusses the problems that arose during implementation.
Finally, the chapter concludes with the testing strategy and the continuous-integration setup that ensures the codebase keeps working as future contributors extend it.

The implementation follows the design chapter without major deviation, and the requirements defined in the analysis are fulfilled by the resulting program.

#include "backend.typ"

#include "frontend.typ"

#include "sequence.typ"

#include "api.typ"

#include "algorithms.typ"

#include "problem-discussion.typ"

#include "testing.typ"
