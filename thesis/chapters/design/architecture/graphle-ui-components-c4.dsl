workspace "GraphleUI" "C4 component diagram of the GraphleUI client" {

    model {
        user = person "User, Person" {
            tags "Person"
        }

        manager = softwareSystem "GraphleManager, Backend container" {
            tags "ManagerContainer"
        }

        graphleUI = softwareSystem "GraphleUI" "Kotlin Compose Multiplatform desktop client" {
            uiContainer = container "GraphleUI Client" "Kotlin Compose Multiplatform" {
                app = component "Application root, Main.kt, App.kt" {
                    tags "Presentation"
                }
                state = component "Displayed state, DisplayedSettings, DisplayedData, DisplayMode" {
                    tags "Presentation"
                }
                header = component "Header module, Header, CommandLine, AppMenu" {
                    tags "Presentation"
                }
                dialogs = component "Dialog module, AddTag, AddRelationship, AddFile, MoveFile, DeleteFile" {
                    tags "Presentation"
                }
                bodies = component "Body and feature views, DisplayedBody, FileBody, FilenameBody, FilesWithTagBody, TagsView" {
                    tags "Presentation"
                }

                fileFetcher = component "File loading, FileFetcher" {
                    tags "AppService"
                }
                graphqlCommands = component "GraphQL command wrapper, GraphQLCommands, Apollo generated operations" {
                    tags "AppService"
                }
                dslHandler = component "DSL response handling, DSLCommandHandler, DSLHistory" {
                    tags "AppService"
                }
                fileUtil = component "File utility layer, FileUtil, open/download helpers" {
                    tags "AppService"
                }

                apollo = component "Apollo GraphQL client, /graphql" {
                    tags "Transport"
                }
                rest = component "Ktor REST client, /dsl, /download" {
                    tags "Transport"
                }
                websocket = component "Ktor WebSocket client, /ws autocomplete" {
                    tags "Transport"
                }
            }
        }

        user -> app "uses desktop window"
        app -> state "owns mutable state"
        app -> header "renders"
        app -> dialogs "renders"
        app -> bodies "renders selected mode"

        header -> websocket "autocomplete prefix"
        header -> dslHandler "submit DSL command"
        dslHandler -> rest "POST command"
        dslHandler -> state "maps DSLResponse"

        dialogs -> graphqlCommands "mutations"
        dialogs -> fileFetcher "refresh current file"
        bodies -> fileFetcher "navigate files"
        bodies -> graphqlCommands "remove tag/relationship, query by tag"
        bodies -> fileUtil "open file"

        fileFetcher -> graphqlCommands "fileByLocation"
        graphqlCommands -> apollo "typed operations"
        fileUtil -> rest "remote download"

        apollo -> manager "GraphQL"
        rest -> manager "REST"
        websocket -> manager "WebSocket"
    }

    views {
        component uiContainer "Components" {
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
            element "ManagerContainer" {
                background #e3f2fd
                color #1e5f8f
            }
            element "Presentation" {
                background #e8f5e9
                color #2e7d32
            }
            element "AppService" {
                background #fffde7
                color #8a7b1f
            }
            element "Transport" {
                background #e3f2fd
                color #1e5f8f
            }
        }
    }

}