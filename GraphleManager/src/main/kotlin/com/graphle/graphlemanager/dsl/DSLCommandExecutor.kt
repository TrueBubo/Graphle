package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.FileService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

/**
 * Responsible for executing DSL scopes against the Neo4j database.
 * Handles query execution, result mapping, and special relationship keywords.
 */
@Component
class DSLCommandExecutor(
    private val neo4jClient: Neo4jClient,
    private val cypherQueryBuilder: CypherQueryBuilder
) {

    /**
     * Executes a find command by processing scopes sequentially.
     * @param scopes The list of scopes to execute
     * @return The response containing execution results
     */
    fun executeFindCommand(scopes: List<Scope>): DSLResponse {
        if (scopes.isEmpty()) return createErrorResponse("No scopes to execute")

        var prevSelectedFilenames: List<AbsolutePathString>? = null
        var response: DSLResponse = createErrorResponse("No scopes executed")

        for (scope in scopes) {
            response = executeFindScope(scope, prevSelectedFilenames).run {
                DSLResponse(type, responseObject.distinct())
            }
            prevSelectedFilenames = when (response.type) {
                ResponseType.FILENAMES -> {
                    response.responseObject
                }

                ResponseType.CONNECTIONS -> {
                    response.responseObject.map {
                        val connection = Json.decodeFromString<Connection>(it)
                        connection.to
                    }
                }

                ResponseType.ERROR, ResponseType.SUCCESS, ResponseType.FILE, ResponseType.TAG -> return response
            }
        }
        return response
    }

    /**
     * Executes a scope and returns the results.
     * @param scope The scope to execute
     * @param prevSelectedFilenames Previously selected filenames to filter on
     * @return The response containing execution results
     */
    fun executeFindScope(
        scope: Scope,
        prevSelectedFilenames: List<AbsolutePathString>?
    ): DSLResponse {
        val additionalConnections: MutableList<Connection> = mutableListOf()

        val ranScope = when (scope.entityType) {
            EntityType.Relationship -> {
                val tokens = processRelationshipTokens(scope.text, additionalConnections, prevSelectedFilenames)
                Scope(
                    entityType = scope.entityType,
                    text = tokens.joinToString(separator = " ")
                )
            }

            EntityType.File -> scope
        }

        val query = cypherQueryBuilder.convertScopeToQuery(ranScope, prevSelectedFilenames)
            ?: return createErrorResponse("Failed to build query for scope: ${scope.text}")

        return when (scope.entityType) {
            EntityType.File -> DSLResponse(
                type = ResponseType.FILENAMES,
                responseObject =
                if (ranScope.text == "" && prevSelectedFilenames != null) prevSelectedFilenames.map { it }
                else neo4jClient.query(query)
                    .bindAll(mapOf("locations" to (prevSelectedFilenames ?: emptyList())))
                    .fetchAs(Filename::class.java)
                    .mappedBy { _, record -> Filename(record["f"].asNode().get("location").asString()) }
                    .all()
                    .map { it.path }
            )

            EntityType.Relationship -> DSLResponse(
                type = ResponseType.CONNECTIONS,
                responseObject = neo4jClient.query(query)
                    .bindAll(mapOf("locations" to (prevSelectedFilenames ?: emptyList())))
                    .fetchAs(Connection::class.java)
                    .mappedBy { _, record ->
                        Connection(
                            from = record["f1"].asNode().get("location").asString(),
                            to = record["f2"].asNode().get("location").asString(),
                            name = record["r"].asRelationship().get("name").asString(),
                            value = record["r"].asRelationship().get("value").asString()
                        )
                    }.all()
                    .plus(additionalConnections)
                    .map { Json.encodeToString<Connection>(it) }
            )
        }
    }

    /**
     * Processes relationship tokens and handles special keywords like DESC and PRED.
     * @param scopeText The text within the relationship scope
     * @param additionalConnections Mutable list to add additional connections to
     * @param prevSelectedFilenames Previously selected filenames
     * @return Processed list of tokens
     */
    private fun processRelationshipTokens(
        scopeText: String,
        additionalConnections: MutableList<Connection>,
        prevSelectedFilenames: List<AbsolutePathString>?
    ): List<String> {
        var tokens = splitIntoTokens(scopeText)
        val firstTokens = listOfNotNull(tokens.getOrNull(0), tokens.getOrNull(1))
        val additionalFilenameGetter = listOf("DESC", "PRED")

        if (firstTokens.isNotEmpty() && firstTokens.first().uppercase() in additionalFilenameGetter) {
            tokens = tokens.drop(1)
            addAdditionalFilenames(firstTokens.first(), additionalConnections, prevSelectedFilenames)
        }
        if (firstTokens.size > 1 && firstTokens[1].uppercase() in additionalFilenameGetter) {
            tokens = tokens.drop(1)
            addAdditionalFilenames(firstTokens[1], additionalConnections, prevSelectedFilenames)
        }
        return tokens
    }

    /**
     * Adds additional filenames based on special keywords (DESC/PRED).
     * @param getter The keyword type (DESC for descendants, PRED for predecessors)
     * @param additionalConnections Mutable list to add connections to
     * @param prevSelectedFilenames Previously selected filenames to process
     */
    private fun addAdditionalFilenames(
        getter: String,
        additionalConnections: MutableList<Connection>,
        prevSelectedFilenames: List<AbsolutePathString>?
    ) {
        if (prevSelectedFilenames != null) {
            when (getter.uppercase()) {
                "DESC" -> additionalConnections.addAll(
                    prevSelectedFilenames
                        .flatMap { from ->
                            FileService.descendantsOfFile(from).map { to ->
                                Connection(
                                    from = from,
                                    to = to,
                                    name = "descendant",
                                )
                            }
                        }
                )

                "PRED" -> additionalConnections.addAll(
                    prevSelectedFilenames
                        .mapNotNull { child ->
                            FileService.parentOfFile(child)?.let { parent ->
                                Connection(
                                    from = child,
                                    to = parent,
                                    name = "parent",
                                )
                            }
                        }
                )
            }
        }
    }

    /**
     * Creates an error response with the given message.
     * @param message The error message
     * @return A DSLResponse with error type
     */
    private fun createErrorResponse(message: String): DSLResponse =
        DSLResponse(ResponseType.ERROR, listOf(message))
}