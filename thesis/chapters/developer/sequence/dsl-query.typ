#import "shared.typ": seq-diagram

#let dsl-query-diagram() = seq-diagram(
  caption: [Executing a DSL `find` query.],
  actors: ("Client", "DSLController", "DSLInterpreter", "Scope/Cypher", "Executor", "Neo4j"),
  steps: (
    (from: 0, to: 1, label: [`POST /dsl { command }`]),
    (from: 1, to: 2, label: [`interpret(command)`]),
    (from: 2, to: 3, label: [`splitSearchIntoScopes`]),
    (from: 3, to: 2, label: [`List<Scope>`], kind: "return"),
    (from: 2, to: 3, label: [`CypherQueryBuilder.build`]),
    (from: 3, to: 2, label: [Cypher query], kind: "return"),
    (from: 2, to: 4, label: [`executeFindCommand(scopes)`]),
    (from: 4, to: 5, label: [run Cypher via `Neo4jClient`]),
    (from: 5, to: 4, label: [rows], kind: "return"),
    (from: 4, to: 2, label: [`DSLResponse(type, ...)`], kind: "return"),
    (from: 2, to: 1, label: [`DSLResponse`], kind: "return"),
    (from: 1, to: 0, label: [JSON response], kind: "return"),
  ),
)
