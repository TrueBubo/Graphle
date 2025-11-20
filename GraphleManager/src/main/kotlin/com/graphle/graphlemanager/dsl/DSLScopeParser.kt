package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Component

/**
 * Responsible for parsing DSL command strings into structured scopes.
 * Handles quote escaping, parenthesis nesting, and scope identification.
 */
@Component
class DSLScopeParser {
    /**
     * Splits a search command into individual scopes.
     * Parses the command to identify file scopes (enclosed in parentheses) and relationship scopes (enclosed in brackets).
     * @param command The search command to split
     * @return List of scopes found in the command
     */
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
}