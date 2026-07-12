workspace "GraphleManager" "C4 component diagram of the GraphleManager backend, mirroring graphle-manager-components-c4.dot" {

    model {
        client = person "GraphleUI or DSL client, External client" {
            tags "External"
        }

        filesystem = softwareSystem "Operating filesystem, External system" {
            tags "External"
        }
        neo4j = softwareSystem "Neo4j, Database" {
            tags "Database"
        }
        valkey = softwareSystem "Valkey, Database" {
            tags "Database"
        }

        graphleManager = softwareSystem "GraphleManager" "Coordinates filesystem operations, the Neo4j graph, and the Valkey-backed autocomplete index" {
            backend = container "GraphleManager Backend" "Spring Boot application" "Kotlin, Spring Boot" {
                graphqlApi = component "GraphQL controllers, FileController, TagController, ConnectionController" {
                    tags "ApiAdapter"
                }
                restApi = component "REST controllers, DSLController, FileDownloadController" {
                    tags "ApiAdapter"
                }
                websocketApi = component "WebSocket adapter, DSLAutoCompleterHandler, SessionRegistry" {
                    tags "ApiAdapter"
                }

                fileModule = component "File module, FileService, file model" {
                    tags "DomainModule"
                }
                tagModule = component "Tag module, TagService, tag model" {
                    tags "DomainModule"
                }
                connectionModule = component "Connection module, ConnectionService, connection model" {
                    tags "DomainModule"
                }
                dslModule = component "DSL module, DSLInterpreter, DSLScopeParser, DSLTokenInterpreter, DSLCommandExecutor, CypherQueryBuilder" {
                    tags "DomainModule"
                }
                autocompleteModule = component "Autocomplete module, DSLAutoCompleter, FilenameCompleter, ConcurrentCache" {
                    tags "DomainModule"
                }
                sweeperModule = component "Background maintenance, Neo4JSweeper" {
                    tags "DomainModule"
                }

                fileRepo = component "File repository, FileRepository" {
                    tags "PersistenceAdapter"
                }
                tagRepo = component "Tag repository, TagRepository" {
                    tags "PersistenceAdapter"
                }
                connectionRepo = component "Connection repository, ConnectionRepository" {
                    tags "PersistenceAdapter"
                }
                valkeyAdapter = component "Valkey autocomplete adapter, ValkeyFilenameCompleterService, JedisStorage" {
                    tags "PersistenceAdapter"
                }
            }
        }

        client -> graphqlApi "/graphql"
        client -> restApi "/dsl, /download"
        client -> websocketApi "/ws"

        graphqlApi -> fileModule "file queries/mutations"
        graphqlApi -> tagModule "tag queries/mutations"
        graphqlApi -> connectionModule "relationship mutations"
        restApi -> dslModule "interpret command"
        restApi -> filesystem "stream file"
        websocketApi -> autocompleteModule "complete prefix"

        dslModule -> fileModule "file commands"
        dslModule -> tagModule "tag commands"
        dslModule -> connectionModule "relationship commands"
        dslModule -> fileRepo "find via Cypher"
        dslModule -> tagRepo "find via Cypher"
        dslModule -> connectionRepo "find via Cypher"

        fileModule -> fileRepo "file graph updates"
        fileModule -> filesystem "read hierarchy, write files"
        fileModule -> autocompleteModule "insert visited paths"
        tagModule -> tagRepo "tag graph updates"
        connectionModule -> connectionRepo "relationship graph updates"
        sweeperModule -> fileRepo "remove stale nodes"
        sweeperModule -> tagRepo "remove stale nodes"
        sweeperModule -> filesystem "check existence"

        autocompleteModule -> valkeyAdapter "trie operations"
        fileRepo -> neo4j "Cypher calls"
        tagRepo -> neo4j "Cypher calls"
        connectionRepo -> neo4j "Cypher calls"
        valkeyAdapter -> valkey "Valkey calls"
    }

    views {
        component backend "Components" {
            include *
            autoLayout lr
        }

        styles {
            element "Element" {
                fontSize 36
            }
            relationship "Relationship" {
                fontSize 56
            }
            element "Person" {
                shape person
                background #fff7dd
                color #8a6d3b
            }
            element "External" {
                background #eeeeee
                color #737373
            }
            element "Database" {
                shape cylinder
                background #f3e8ff
                color #6a3ea1
            }
            element "ApiAdapter" {
                background #e3f2fd
                color #1e5f8f
            }
            element "DomainModule" {
                background #e8f5e9
                color #2e7d32
            }
            element "PersistenceAdapter" {
                background #fffde7
                color #8a7b1f
            }
        }
    }
}