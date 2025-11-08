package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.FileService
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service

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


@Service
class DSLInterpreter(private val neo4jClient: Neo4jClient) {
    var prevSelectedFilenames: List<AbsolutePathString>? = null

    fun executeScope(
        scope: Scope,
        prevSelectedFilenames: List<AbsolutePathString>? = this.prevSelectedFilenames
    ): List<Filename> {
        val additionalFilenames: MutableList<AbsolutePathString> = mutableListOf()

        val ranScope = when (scope.entityType) {
            EntityType.Relationship -> {
                var tokens = splitIntoTokens(scope.text)
                val firstTokens = listOfNotNull(tokens.getOrNull(0), tokens.getOrNull(1))
                val additionalFilenameGetter = listOf("DESC", "PRED")
                val addAdditionalFilenames = { getter: String ->
                    if (prevSelectedFilenames != null) {
                        when (getter.uppercase()) {
                            "DESC" -> additionalFilenames.addAll(
                                prevSelectedFilenames
                                    .flatMap { FileService.descendantsOfFile(it) })

                            "PRED" -> additionalFilenames.addAll(
                                prevSelectedFilenames
                                    .mapNotNull { FileService.parentOfFile(it) })
                        }
                    }
                }
                if (firstTokens.isNotEmpty() && firstTokens.first().uppercase() in additionalFilenameGetter) {
                    tokens = tokens.drop(1)
                    addAdditionalFilenames(firstTokens.first())
                }
                if (firstTokens.size > 1 && firstTokens[1].uppercase() in additionalFilenameGetter) {
                    tokens = tokens.drop(1)
                    addAdditionalFilenames(firstTokens[1])
                }
                Scope(
                    entityType = scope.entityType,
                    text = tokens.joinToString(separator = " ")
                )
            }

            EntityType.File -> scope
        }

        val query = convertScopeToCommand(ranScope) ?: throw IllegalArgumentException("Scope $scope not found")
        return neo4jClient.query(query)
            .bindAll(mapOf("locations" to (prevSelectedFilenames ?: emptyList())))
            .also { this.prevSelectedFilenames = null }
            .fetchAs(Filename::class.java)
            .mappedBy { _, record -> Filename(record["f"].asNode().get("location").asString()) }
            .all()
            .toList()
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
                            scope = null;
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
        prevSelectedFilename: List<AbsolutePathString>? = prevSelectedFilenames
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
        val processedTokens: List<String> = tokens.map { token ->
            if (token == HIGHER_PRIORITY_OPENING_TOKEN || token == HIGHER_PRIORITY_CLOSING_TOKEN) {
                tokenType = TokenType.first()
                return@map token
            }
            when (tokenType) {
                TokenType.VARIABLE_NAME -> {
                    tokenType = tokenType.next()
                    return@map processFileVariableName(token) ?: return null
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

        val whereCondition = processedTokens.joinToString(" ")
        return (if (prevSelectedFilenames == null) "" else "UNWIND \$locations as loc") +
                "MATCH (f:File) OPTIONAL MATCH (f)-[:HasTag]-(t:Tag) WITH f, t" +
                (if (prevSelectedFilenames == null) "" else ",collect(loc) as locs") +
                " WHERE ${if (prevSelectedFilenames == null) "" else "f.location in locs AND "}$whereCondition RETURN f".trimMargin()

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

        val whereCondition = processedTokens.joinToString(" ")
        return (if (prevSelectedFilenames == null) "" else "UNWIND \$locations as loc") +
                "MATCH (f1:File)-[r:HasTag]-(f2:File) WITH r, f1, f2" +
                (if (prevSelectedFilenames == null) "" else ",collect(loc) as locs") +
                " WHERE ${if (prevSelectedFilenames == null) "" else "f1.location in locs AND "}$whereCondition RETURN f2".trimMargin()

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