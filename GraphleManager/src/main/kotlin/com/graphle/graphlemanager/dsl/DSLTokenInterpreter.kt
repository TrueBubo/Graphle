package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.commons.normalize
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.dsl.DSLUtil.removeQuotes
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.tag.TagInput
import org.springframework.stereotype.Component

/**
 * Responsible for interpreting and parsing DSL tokens into structured inputs.
 * Handles validation and conversion of token lists into domain objects.
 */
@Component
class DSLTokenInterpreter {

    /**
     * Parses tokens for relationship operations.
     * @param tokens The list of tokens to parse
     * @return ConnectionInput if parsing succeeds, null otherwise
     */
    fun parseRelationshipTokens(tokens: List<String>): ConnectionInput? {
        if (tokens.size !in 3..4) return null
        return ConnectionInput(
            from = tokens[0].removeQuotes().normalize(),
            to = tokens[1].removeQuotes().normalize(),
            name = tokens[2].removeQuotes(),
            value = tokens.getOrNull(3)?.removeQuotes(),
            bidirectional = false
        )
    }

    /**
     * Parses tokens for tag modification operations.
     * @param tokens The list of tokens to parse
     * @return TagModificationInput if parsing succeeds, null otherwise
     */
    fun parseTagModificationTokens(tokens: List<String>): TagModificationInput? {
        if (tokens.size !in 2..3) return null
        return TagModificationInput(
            location = tokens[0].removeQuotes().normalize(),
            tag = TagInput(
                name = tokens[1].removeQuotes(),
                value = tokens.getOrNull(2)?.removeQuotes(),
            )
        )
    }

    /**
     * Parses tokens for get operations.
     * @param tokens The list of tokens to parse
     * @return The parsed string if successful, null otherwise
     */
    fun parseGetTokens(tokens: List<String>): String? {
        if (tokens.size != 1) return null
        return tokens.first().removeQuotes()
    }

    /**
     * Parses tokens for detail command operations.
     * @param tokens The list of tokens to parse
     * @return The file path if parsing succeeds, null otherwise
     */
    fun parseDetailTokens(tokens: List<String>): AbsolutePathString? {
        if (tokens.size != 1) return null
        return tokens.first().removeQuotes()
    }
}