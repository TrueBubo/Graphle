#import "../../template/shared.typ": *

== Testing

The backend favors integration tests over isolated unit tests.
#voc("graphql") resolvers and #voc("rest") endpoints are exercised through Spring's `MockMvc` against a real Spring context wired up to live Neo4j and Valkey instances, so behavior observed in tests is the same behavior the production process will produce.
Pure logic such as #voc("dsl") token handling, the autocomplete #voc("trie"), and the concurrent #voc("cache") is still covered by focused unit tests, but anything that touches persistence is verified end-to-end.
Reusable bases (`BaseGraphQlIntegrationTest`, `BaseRestIntegrationTest`) hide the boilerplate of issuing #voc("graphql") operations and #voc("http") requests, and randomized identifiers keep parallel runs from colliding on shared graph state.

=== Autocomplete latency measurement

Autocomplete latency was measured manually from the GUI because the perceived responsiveness of the #voc("dsl") command line depends on the full round trip over #voc("websocket"), not only on the pure trie lookup.
The client timed each autocomplete request from prefix frame send to matching response frame receipt on an already established `/ws` connection.
The samples therefore exclude connection setup and focus on request handling. This includes Valkey lookup, path reconstruction, filesystem checks, response transfer, and client-side parsing.
Test inputs were randomly chosen path prefixes.

#table(
  columns: (auto, auto, auto, auto, auto, auto, auto),
  [Result count], [Samples], [Minimum], [Average], [Median], [95th percentile], [Maximum],
  [0], [21], [0.9 ms], [1.7 ms], [1.5 ms], [3.2 ms], [3.2 ms],
  [1], [29], [5.2 ms], [15.2 ms], [15.8 ms], [26.5 ms], [28.1 ms],
  [5], [40], [33.7 ms], [47.9 ms], [46.6 ms], [64.1 ms], [66.1 ms],
)

Latency mainly increased with the number of valid paths returned.
Prefixes with no result completed almost immediately, while the default five-result case was slower
because it had to collect, reconstruct, validate, and encode more candidates.
Even the slowest measured group stayed well below the 250 ms limit defined by #link(label("qualitative_requirements"))[qualitative requirement Q2.1].

=== Continuous Integration

The GitHub Actions workflow runs on every push and on pull requests targeting `main`.
Neo4j and Valkey are provided as service containers so the same wiring used locally is reproduced in CI.
The automatic action ensures the functionality in `main` will not regress once new pull requests with new features start coming in.
