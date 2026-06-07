#import "../../template/shared.typ": *

== Testing

The backend favours integration tests over isolated unit tests.
GraphQL resolvers and REST endpoints are exercised through Spring's `MockMvc` against a real Spring context wired up to live Neo4j and Valkey instances, so behaviour observed in tests is the same behaviour the production process will produce.
Pure logic such as DSL token handling, the autocomplete trie, and the concurrent cache are is still covered by focused unit tests, but anything that touches persistence is verified end-to-end.
Reusable bases (`BaseGraphQlIntegrationTest`, `BaseRestIntegrationTest`) hide the boilerplate of issuing GraphQL operations and HTTP requests, and randomised identifiers keep parallel runs from colliding on shared graph state.

=== Continuous Integration

The GitHub Actions workflow runs on every push and on pull requests targeting `main`.
Neo4j and Valkey are provided as service containers with health checks so the same wiring used locally is reproduced in CI.
The automatic action ensures the functionality in `main` will not regress once new pull requests with new features start coming in.