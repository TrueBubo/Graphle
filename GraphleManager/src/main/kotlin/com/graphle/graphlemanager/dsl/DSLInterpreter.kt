package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.commons.normalize
import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.dsl.DSLUtil.removeQuotes
import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.FileController
import com.graphle.graphlemanager.file.FileService
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import kotlinx.serialization.Serializable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

data class Scope(val entityType: EntityType, val text: String)

const val FILE_SCOPE_OPENING = '('
const val FILE_SCOPE_CLOSING = ')'
const val RELATIONSHIP_SCOPE_OPENING = '['
const val RELATIONSHIP_SCOPE_CLOSING = ']'

const val HIGHER_PRIORITY_OPENING_TOKEN = "("
const val HIGHER_PRIORITY_CLOSING_TOKEN = ")"

sealed interface ScopeOutput

@JvmInline
value class Filename(val path: AbsolutePathString) : ScopeOutput

enum class ResponseType {
    ERROR,
    SUCCESS,
    FILENAMES,
    CONNECTIONS,
    FILE
}

@Serializable
data class DSLResponse(val type: ResponseType, val responseObject: List<String>)

data class TagModificationInput(val location: AbsolutePathString, val tag: TagInput)

enum class Commands(val command: String) {
    FIND("find"),
    ADD_REL("addRel"),
    REMOVE_REL("removeRel"),
    ADD_TAG("addTag"),
    REMOVE_TAG("removeTag"),
    DETAIL("detail"),
}

@Service
class DSLInterpreter(
    private val neo4jClient: Neo4jClient,
    private val fileService: FileService,
    private val connectionService: ConnectionService,
    private val tagService: TagService,
    private val fileController: FileController
) {

    fun interpret(command: String): DSLResponse {
        val tokens = splitIntoTokens(command)
        if (tokens.isEmpty()) parseError(command)
        return try {
            when (tokens.first()) {
                Commands.FIND.command -> {
                    interpretFind(command.drop(tokens.first().length + 1))
                }

                Commands.ADD_REL.command -> {
                    val connectionInput = interpretConnectionTokens(tokens.drop(1)) ?: return parseError(command)
                    connectionService.addConnection(connectionInput)
                    return DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.REMOVE_REL.command -> {
                    val connectionInput = interpretConnectionTokens(tokens.drop(1)) ?: return parseError(command)
                    connectionService.removeConnection(connectionInput)
                    return DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.ADD_TAG.command -> {
                    val tagInput = interpretTagTokens(tokens.drop(1)) ?: return parseError(command)
                    tagService.addTagToFile(tagInput.location, tagInput.tag)
                    return DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.REMOVE_TAG.command -> {
                    val tagInput = interpretTagTokens(tokens.drop(1)) ?: return parseError(command)
                    System.err.println("Removing tag with value: $tagInput")
                    tagService.removeTag(tagInput.location, tagInput.tag)
                    return DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.DETAIL.command -> {
                    val filename = interpretDetailTokens(tokens.drop(1)) ?: return parseError(command)
                    val detail = fileController.fileByLocation(filename) ?: return DSLResponse(
                        ResponseType.ERROR,
                        listOf("File $filename not found")
                    )
                    return DSLResponse(ResponseType.FILE, listOf(Json.encodeToString(detail)))
                }

                else -> DSLResponse(ResponseType.ERROR, listOf("Unknown command ${tokens.first()}"))

            }
        } catch (_: Exception) {
            return parseError(command)
        }
    }

    private fun parseError(command: String) =
        DSLResponse(ResponseType.ERROR, listOf("Unable to parse $command"))

    fun interpretFind(command: String): DSLResponse {
        var prevSelectedFilenames: List<AbsolutePathString>? = null
        val scopes = splitSearchIntoScopes(command)
        if (scopes.isEmpty()) return parseError(command)

        var response: DSLResponse = parseError(command)
        for (scope in scopes) {
            response = executeScope(scope, prevSelectedFilenames).run { DSLResponse(type, responseObject.distinct()) }
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

                ResponseType.ERROR, ResponseType.SUCCESS, ResponseType.FILE -> return response
            }
        }
        return response
    }

    fun interpretConnectionTokens(tokens: List<String>): ConnectionInput? {
        if (tokens.size !in 3..4) return null
        return ConnectionInput(
            from = tokens[0].removeQuotes().normalize(),
            to = tokens[1].removeQuotes().normalize(),
            name = tokens[2].removeQuotes(),
            value = tokens.getOrNull(3)?.removeQuotes(),
            bidirectional = false
        )
    }

    fun interpretTagTokens(tokens: List<String>): TagModificationInput? {
        if (tokens.size !in 2..3) return null
        return TagModificationInput(
            location = tokens[0].removeQuotes().normalize(),
            tag = TagInput(
                name = tokens[1].removeQuotes(),
                value = tokens.getOrNull(2)?.removeQuotes(),
            )
        )
    }

    fun interpretDetailTokens(tokens: List<String>): AbsolutePathString? {
        if (tokens.size != 1) return null
        return tokens.first().removeQuotes()
    }


    fun executeScope(
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

        val query = convertScopeToCommand(ranScope, prevSelectedFilenames) ?: return parseError(scope.text)
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

    fun splitSearchIntoScopes(command: String): List<Scope> {
        var scope: EntityType? = null
        var scopeStartIndex: Int? = null
        var previousScope: EntityType? = null
        var inQuotes = false
        var isEscaped = false
        val scopes = mutableListOf<Scope>()
        var parenthesisLevel = 0

        command.forEachIndexed { idx, char ->
            if (char == '"' && !isEscaped) {
                inQuotes = !inQuotes
            }
            if (!inQuotes) {
                when (char) {
                    FILE_SCOPE_OPENING -> {
                        if (scope == null && previousScope != EntityType.File) {
                            scope = EntityType.File
                            scopeStartIndex = idx + 1
                        } else {
                            parenthesisLevel++
                        }
                    }

                    FILE_SCOPE_CLOSING -> {
                        if (parenthesisLevel > 0) parenthesisLevel--
                        else if (scope == EntityType.File) {
                            if (scopeStartIndex == null) return emptyList()
                            previousScope = EntityType.File
                            scopes.addLast(
                                Scope(
                                    entityType = EntityType.File,
                                    text = command.substring(scopeStartIndex, idx),
                                )
                            )
                            scopeStartIndex = null
                            scope = null
                        } else return emptyList()
                    }

                    RELATIONSHIP_SCOPE_OPENING -> {
                        if (scope == null && previousScope != EntityType.Relationship && previousScope != null) {
                            scope = EntityType.Relationship
                            scopeStartIndex = idx + 1
                        } else return emptyList()
                    }

                    RELATIONSHIP_SCOPE_CLOSING -> {
                        if (scope == EntityType.Relationship) {
                            if (scopeStartIndex == null) return emptyList()
                            previousScope = EntityType.Relationship
                            scopes.addLast(
                                Scope(
                                    entityType = EntityType.Relationship,
                                    text = command.substring(scopeStartIndex, idx)
                                )
                            )
                            scopeStartIndex = null
                            scope = null
                        }
                    }

                    else -> {
                        if (scope == null) return emptyList()
                    }
                }

                isEscaped = false
                if (char == '\\') {
                    isEscaped = true
                }
            }
        }
        return scopes
    }

    fun convertScopeToCommand(
        scope: Scope,
        prevSelectedFilename: List<AbsolutePathString>?
    ): String? {
        val tokens = splitIntoTokens(scope.text)
        return when (scope.entityType) {
            EntityType.File -> convertFileScopeToCommand(tokens, prevSelectedFilename)
            EntityType.Relationship -> convertRelationshipScopeToCommand(tokens, prevSelectedFilename)
        }
    }

    private fun convertFileScopeToCommand(
        tokens: List<String>,
        prevSelectedFilenames: List<AbsolutePathString>?
    ): String? {
        var tokenType = TokenType.first()
        val mutableTokens = tokens.toMutableList()
        val processedTokens: List<String> = mutableTokens.mapIndexed { index, token ->
            if (token == HIGHER_PRIORITY_OPENING_TOKEN || token == HIGHER_PRIORITY_CLOSING_TOKEN) {
                tokenType = TokenType.first()
                return@mapIndexed token
            }
            when (tokenType) {
                TokenType.VARIABLE_NAME -> {
                    tokenType = tokenType.next()
                    System.err.println("Ensured exist")
                    if (token == "location" && index + 2 < tokens.size) {
                        val normalizedLocation = tokens[index + 2].removeQuotes().normalize()
                        ensureFileNodeExists(location = normalizedLocation)
                        mutableTokens[index + 2] = "\"$normalizedLocation\""
                        System.err.println("added")
                    }
                    return@mapIndexed processFileVariableName(token) ?: return null
                }

                TokenType.OPERATOR -> {
                    tokenType = tokenType.next()
                    return@mapIndexed processOperatorTypes(token) ?: return null
                }

                TokenType.VALUE -> {
                    tokenType = tokenType.next()
                    return@mapIndexed token
                }

                TokenType.CONJUNCTION -> {
                    tokenType = tokenType.next()
                    return@mapIndexed processConjunctionOperators(token) ?: return null
                }
            }
        }

        val whereCondition = (if (prevSelectedFilenames == null) "" else "f.location in locs ") +
                (if (processedTokens.isNotEmpty() && prevSelectedFilenames != null) "AND " else "") +
                processedTokens.joinToString(" ")

        return (if (prevSelectedFilenames == null) "" else $$"UNWIND $locations as loc ") +
                "MATCH (f:File) OPTIONAL MATCH (f)-[:HasTag]-(t:Tag) WITH f, t" +
                (if (prevSelectedFilenames == null) "" else ",collect(loc) as locs ") +
                " ${if (whereCondition.isEmpty()) "" else "WHERE $whereCondition"} RETURN f".trimMargin()

    }

    private fun convertRelationshipScopeToCommand(
        tokens: List<String>,
        prevSelectedFilenames: List<AbsolutePathString>?
    ): String? {
        var tokenType = TokenType.first()
        val processedTokens: List<String> = tokens.map { token ->
            if (token == HIGHER_PRIORITY_OPENING_TOKEN || token == HIGHER_PRIORITY_CLOSING_TOKEN) {
                tokenType = TokenType.first()
                return@map token
            }
            when (tokenType) {
                TokenType.VARIABLE_NAME -> {
                    tokenType = tokenType.next()
                    return@map processRelationshipVariableName(token) ?: return null
                }

                TokenType.OPERATOR -> {
                    tokenType = tokenType.next()
                    return@map processOperatorTypes(token) ?: return null
                }

                TokenType.VALUE -> {
                    tokenType = tokenType.next()
                    return@map token
                }

                TokenType.CONJUNCTION -> {
                    tokenType = tokenType.next()
                    return@map processConjunctionOperators(token) ?: return null
                }
            }
        }

        val whereCondition = (if (prevSelectedFilenames == null) "" else "f1.location in locs ") +
                (if (processedTokens.isNotEmpty()) "AND " else "") +
                processedTokens.joinToString(" ")
        return (if (prevSelectedFilenames == null) "" else $$"UNWIND $locations as loc ") +
                "MATCH (f1:File)-[r:Relationship]->(f2:File) WITH r, f1, f2" +
                (if (prevSelectedFilenames == null) "" else ",collect(loc) as locs ") +
                " ${if (whereCondition.isEmpty()) "" else "WHERE $whereCondition"} RETURN f1, r, f2".trimMargin()

    }

    private fun ensureFileNodeExists(location: AbsolutePathString) {
        fileService.addFileNode(location)
    }

    private fun processFileVariableName(variableName: String): String? = when (variableName) {
        "location" -> "f.location"
        "tagName" -> "t.name"
        "tagValue" -> "t.value"
        else -> null
    }

    private fun processRelationshipVariableName(variableName: String): String? = when (variableName) {
        "name" -> "r.name"
        "value" -> "r.value"
        else -> null
    }

    private fun processOperatorTypes(operatorType: String): String? = when (operatorType) {
        "=" -> "="
        ">" -> ">"
        ">=" -> ">="
        "<" -> "<"
        "<=" -> "<="
        "!=" -> "<>"
        "<>" -> "<>"
        else -> null
    }

    private fun processConjunctionOperators(operatorType: String): String? = when (operatorType.uppercase()) {
        "AND" -> "AND"
        "OR" -> "OR"
        else -> null
    }
}