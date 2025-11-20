package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.commons.normalize
import com.graphle.graphlemanager.dsl.DSLUtil.ensureQuoted
import com.graphle.graphlemanager.dsl.DSLUtil.removeQuotes
import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.FileService
import org.springframework.stereotype.Component

/**
 * Responsible for building Neo4j Cypher queries from DSL scopes.
 * Handles query construction for both file and relationship scopes.
 */
@Component
class CypherQueryBuilder(
    private val fileService: FileService
) {

    /**
     * Converts a scope into a Neo4j Cypher query command.
     * @param scope The scope to convert
     * @param prevSelectedFilename Previously selected filenames to filter on
     * @return The Cypher query string, or null if conversion fails
     */
    fun convertScopeToQuery(
        scope: Scope,
        prevSelectedFilename: List<AbsolutePathString>?
    ): String? {
        val tokens = splitIntoTokens(scope.text)
        return when (scope.entityType) {
            EntityType.File -> buildFileQuery(tokens, prevSelectedFilename)
            EntityType.Relationship -> buildRelationshipQuery(tokens, prevSelectedFilename)
        }
    }

    /**
     * Converts a file scope into a Neo4j Cypher query.
     * @param tokens The tokens within the file scope
     * @param prevSelectedFilenames Previously selected filenames to filter on
     * @return The Cypher query string, or null if conversion fails
     */
    fun buildFileQuery(
        tokens: List<String>,
        prevSelectedFilenames: List<AbsolutePathString>?
    ): String? {
        var tokenType = TokenType.first()
        val mutableTokens = tokens.toMutableList()
        var lastVariableName: String? = null
        var lastOperator: String? = null

        val processedTokens: List<String> = mutableTokens.mapIndexed { index, token ->
            if (token == HIGHER_PRIORITY_OPENING_TOKEN || token == HIGHER_PRIORITY_CLOSING_TOKEN) {
                tokenType = TokenType.first()
                lastVariableName = null
                lastOperator = null
                return@mapIndexed token
            }
            when (tokenType) {
                TokenType.VARIABLE_NAME -> {
                    tokenType = tokenType.next()
                    lastVariableName = token

                    if (token == "location" && index + 2 < tokens.size) {
                        val normalizedLocation = tokens[index + 2].removeQuotes().normalize()
                        ensureFileNodeExists(location = normalizedLocation)
                        mutableTokens[index + 2] = normalizedLocation.ensureQuoted()
                    }

                    val processedName = mapFileVariableName(token) ?: return null

                    // Check if next token is a numeric comparison operator
                    val nextOperator = tokens.getOrNull(index + 1)
                    val isNumericComparison = nextOperator in listOf(">", "<", ">=", "<=")

                    // Wrap tagValue with toFloat() for numeric comparisons
                    return@mapIndexed if (token == "tagValue" && isNumericComparison) {
                        "toFloat($processedName)"
                    } else {
                        processedName
                    }
                }

                TokenType.OPERATOR -> {
                    tokenType = tokenType.next()
                    lastOperator = token
                    return@mapIndexed mapOperator(token) ?: return null
                }

                TokenType.VALUE -> {
                    tokenType = tokenType.next()

                    // For numeric comparisons on tagValue, don't quote the value
                    val isNumericComparison = lastOperator in listOf(">", "<", ">=", "<=")
                    return@mapIndexed if (lastVariableName == "tagValue" && isNumericComparison) {
                        token.removeQuotes()
                    } else {
                        token.ensureQuoted()
                    }
                }

                TokenType.CONJUNCTION -> {
                    tokenType = tokenType.next()
                    lastVariableName = null
                    lastOperator = null
                    return@mapIndexed mapConjunction(token) ?: return null
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

    /**
     * Converts a relationship scope into a Neo4j Cypher query.
     * @param tokens The tokens within the relationship scope
     * @param prevSelectedFilenames Previously selected filenames to filter on
     * @return The Cypher query string, or null if conversion fails
     */
    fun buildRelationshipQuery(
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
                    return@map mapRelationshipVariableName(token) ?: return null
                }

                TokenType.OPERATOR -> {
                    tokenType = tokenType.next()
                    return@map mapOperator(token) ?: return null
                }

                TokenType.VALUE -> {
                    tokenType = tokenType.next()
                    return@map token
                }

                TokenType.CONJUNCTION -> {
                    tokenType = tokenType.next()
                    return@map mapConjunction(token) ?: return null
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

    /**
     * Ensures a file node exists in the database for the given location.
     * @param location The file location to ensure exists
     */
    private fun ensureFileNodeExists(location: AbsolutePathString) {
        fileService.addFileNode(location)
    }

    /**
     * Maps a file variable name to its Cypher property reference.
     * @param variableName The variable name to map
     * @return The Cypher property reference, or null if variable is not recognized
     */
    private fun mapFileVariableName(variableName: String): String? = when (variableName) {
        "location" -> "f.location"
        "tagName" -> "t.name"
        "tagValue" -> "t.value"
        else -> null
    }

    /**
     * Maps a relationship variable name to its Cypher property reference.
     * @param variableName The variable name to map
     * @return The Cypher property reference, or null if variable is not recognized
     */
    private fun mapRelationshipVariableName(variableName: String): String? = when (variableName) {
        "name" -> "r.name"
        "value" -> "r.value"
        else -> null
    }

    /**
     * Maps a DSL operator to its Cypher equivalent.
     * @param operator The operator to map
     * @return The Cypher operator, or null if operator is not recognized
     */
    private fun mapOperator(operator: String): String? = when (operator) {
        "=" -> "="
        ">" -> ">"
        ">=" -> ">="
        "<" -> "<"
        "<=" -> "<="
        "!=" -> "<>"
        "<>" -> "<>"
        else -> null
    }

    /**
     * Maps a conjunction operator to its Cypher equivalent.
     * @param operator The conjunction operator to map
     * @return The Cypher conjunction operator, or null if operator is not recognized
     */
    private fun mapConjunction(operator: String): String? = when (operator.uppercase()) {
        "AND" -> "AND"
        "OR" -> "OR"
        else -> null
    }
}