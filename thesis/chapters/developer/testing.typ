#import "../../template/shared.typ": *

== Testing

The backend favors integration tests over isolated unit tests.
#link(label("voc_graphql"))[GraphQL] resolvers and #link(label("voc_rest"))[REST] endpoints are exercised through Spring's `MockMvc` against a real Spring context wired up to live Neo4j and Valkey instances, so behavior observed in tests is the same behavior the production process will produce.
Pure logic such as #link(label("voc_dsl"))[DSL] token handling, the autocomplete #link(label("voc_trie"))[trie], and the concurrent #link(label("voc_cache"))[cache] is still covered by focused unit tests, but anything that touches persistence is verified end-to-end.
Reusable bases (`BaseGraphQlIntegrationTest`, `BaseRestIntegrationTest`) hide the boilerplate of issuing #link(label("voc_graphql"))[GraphQL] operations and #link(label("voc_http"))[HTTP] requests, and randomized identifiers keep parallel runs from colliding on shared graph state.

=== Autocomplete latency measurement

Autocomplete latency was measured manually from the GUI because the perceived responsiveness of the #link(label("voc_dsl"))[DSL] command line depends on the full #link(label("voc_websocket"))[WebSocket] round trip, not only on the pure trie lookup.
The client recorded a timestamp immediately before sending an autocomplete prefix frame and printed the elapsed time when the corresponding response frame arrived.
The measurement was taken on an already established `/ws` connection, so the numbers exclude TCP and WebSocket handshake cost and focus on request handling, Valkey lookup, path reconstruction, filesystem existence checks, response encoding, transfer, and client-side parsing.

The test input was produced by typing randomly chosen path prefixes.

#table(
  columns: (auto, auto, auto, auto, auto, auto, auto),
  [Result count], [Samples], [Minimum], [Average], [Median], [95th percentile], [Maximum],
  [0], [21], [0.9 ms], [1.7 ms], [1.5 ms], [3.2 ms], [3.2 ms],
  [1], [29], [5.2 ms], [15.2 ms], [15.8 ms], [26.5 ms], [28.1 ms],
  [5], [40], [33.7 ms], [47.9 ms], [46.6 ms], [64.1 ms], [66.1 ms],
)

The results show that autocomplete latency is primarily correlated with how many valid paths the backend has to return.
Prefixes with no matching file path returned almost immediately, because lookup can stop without reconstructing and validating candidates.
Prefixes with a single result were slower, but still below 30 ms in the measured sample.
The default five-result case dominated the slower group. It required collecting multiple candidates, reconstructing full paths, checking that the files still existed, and serializing a larger response.
This explains why otherwise similar prefixes can differ substantially in latency depending on whether they return zero, one, or five suggestions.

Even the slowest measured group stayed well below the 250 ms limit defined by #link(label("qualitative_requirements"))[qualitative requirement Q2.1].
The measured latency therefore leaves room for a larger indexed search space or slower hardware while still satisfying the required autocomplete responsiveness.

=== Continuous Integration

The GitHub Actions workflow runs on every push and on pull requests targeting `main`.
Neo4j and Valkey are provided as service containers with health checks so the same wiring used locally is reproduced in CI.
The automatic action ensures the functionality in `main` will not regress once new pull requests with new features start coming in.
