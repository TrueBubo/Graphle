workspace "Graphle Workspace" "This workspace documents the architecture of the Graphle file manager." {
    model {
        # SW Systems
        Graphle = softwareSystem "Graphle" "Handles managing interactions between individual files" {

            Group "Graphle" {
                GraphleUI = container "Graphle UI" "Displays UI for files and connections" "" "Desktop Front-End" {
                    GraphleDesktopApp = component "Graphle desktop app" "Enables users to graphically interact with the Graphle server"
                    GraphleAPIService = component "Graphle API service" "Handles communication between the front-end interface and the backend server"
                }
                GraphleManager = container "Graphle Manager" "Manages information about files and their relationships and tags" {
                    FileDataController = component "File Data Controller" "Creating and retrieving of files"
                    FileDBController = component "File DB Controller" "Communication with the file database"
                    FileModel = component "File Model" "Handles logic for files"

                    RelationshipDataController = component "Relationship Data Controller" "Creating and retrieving of relationships"
                    RelationshipDBController = component "Relationship DB Controller" "Communication with the relationship database"
                    RelationshipModel = component "Relationship Model" "Handles logic for relationships"

                    TagDataController = component "Tag Data Controller" "Creating and retrieving of tags"
                    TagDBController = component "Tag DB Controller" "Communication with the tag database"
                    TagModel = component "Tag Model" "Handles logic for tags"

                    AutocompleterDataController = component "Autocompleter Data Controller" "DSL autocomplete data Controller"
                    AutocompleterModel = component "Autocompleter Model" "Handles logic for autocompletion"

                    Autocompleter = component "Autocompleter" "Recommends possible ways how to continue in a command"
                }

            }
        }

        ConnectionsDB = softwareSystem "Neo4J" "Stores relationships between files and file tags" "Database"

        FileSystem = softwareSystem "File system" "Handles file interaction with disk" "Existing System"

        # actors
        user = person "User" "Manages the filesystem"

        # relationships between users and the Graphle system
        user -> Graphle "Enters interactions between files"
        Graphle -> user "Reads interations between files"

        user -> GraphleUI "Create and views relationships and tags"

        # Relationships between SW Systems
        Graphle -> ConnectionsDB "Updates relationships and tags"
        ConnectionsDB -> Graphle "Reads relationships and tags"

        Graphle -> FileSystem "Updates files"
        FileSystem -> Graphle "Reads information about the current state"

        # Relationships inside Graphle
        GraphleUI -> GraphleManager "Sends requests for data"
        GraphleManager -> GraphleUI "Receives requested data"
        GraphleManager -> ConnectionsDB "Updating of connections and tags"
        ConnectionsDB -> GraphleManager "Reading of connections and tags"
        GraphleManager -> FileSystem "Updates files"
        FileSystem -> GraphleManager "Reads the current state"


        #Relationships inside GraphleManager
        GraphleUI -> RelationshipDataController "Sends request for creating a relationship"
        RelationshipDataController -> RelationshipModel "Request updating the database with a new connection"
        RelationshipModel -> RelationshipDBController "Request saving / reading data"
        RelationshipDBController -> ConnectionsDB "Saves changes / reads DB"

        GraphleUI -> TagDataController "Sends request for creating a tag"
        TagDataController -> TagModel "Request updating the database with a new tag"
        TagModel -> TagDBController "Request saving / reading data"
        TagDBController -> ConnectionsDB "Saves changes / reads DB"

        GraphleUI -> AutocompleterDataController "Sends request for possible continuations of a command"
        AutocompleterDataController -> AutocompleterModel "Request possible continuations of a command"
        AutocompleterModel -> Autocompleter "Reads possible continuations of a command"
        AutocompleterModel -> Autocompleter "Sends info about invalid continuations"
        Autocompleter -> AutocompleterModel "Provides possible ways how to complete the current term"

        GraphleUI -> FileDataController "Sends request for possible files"
        FileDataController -> FileModel "Handles logic for files"
        FileModel -> FileDBController "Request saving / reading data"
        FileModel -> FileDBController "Updates with new or deletes files"
        FileDBController -> ConnectionsDB "Saves changes / reads DB"

        # Relationships inside GraphleUI
        GraphleDesktopApp -> GraphleAPIService "Sends requests for files and connections"
        GraphleDesktopApp -> GraphleAPIService "Sends requests for autocompletion"
        GraphleAPIService -> GraphleManager "Sends requests for files and connections"
        GraphleAPIService -> GraphleManager "Sends requests for autocompletion"
        user -> GraphleDesktopApp "Requests files and their connections"


    }

    views {
        systemContext Graphle "GraphleSystemDiagram" {
            include *
        }

        container Graphle "GraphleContainerDiagram" {
            include *
        }

        component GraphleUI "GraphleUIComponentDiagram" {
            include *
        }

        component GraphleManager "GraphleManagerComponentDiagram" {
            include *
        }

        theme default

        styles {
            element "Existing System" {
                background #999999
                color #ffffff
            }
            element "Database" {
                background #8243d5
                shape Cylinder
            }
        }
    }
}
