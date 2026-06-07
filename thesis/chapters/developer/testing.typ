#import "../../template/shared.typ": *

== Testing

The backend favours integration tests over isolated unit tests.
#link(label("voc_graphql"))[GraphQL] resolvers and #link(label("voc_rest"))[REST] endpoints are exercised through Spring's `MockMvc` against a real Spring context wired up to live Neo4j and Valkey instances, so behaviour observed in tests is the same behaviour the production process will produce.
Pure logic such as #link(label("voc_dsl"))[DSL] token handling, the autocomplete #link(label("voc_trie"))[trie], and the concurrent #link(label("voc_cache"))[cache] is still covered by focused unit tests, but anything that touches persistence is verified end-to-end.
Reusable bases (`BaseGraphQlIntegrationTest`, `BaseRestIntegrationTest`) hide the boilerplate of issuing #link(label("voc_graphql"))[GraphQL] operations and #link(label("voc_http"))[HTTP] requests, and randomised identifiers keep parallel runs from colliding on shared graph state.

=== Continuous Integration

The GitHub Actions workflow runs on every push and on pull requests targeting `main`.
Neo4j and Valkey are provided as service containers with health checks so the same wiring used locally is reproduced in CI.
The automatic action ensures the functionality in `main` will not regress once new pull requests with new features start coming in.
