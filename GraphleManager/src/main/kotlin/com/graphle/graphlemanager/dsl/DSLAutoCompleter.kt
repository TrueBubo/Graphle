package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import org.springframework.stereotype.Service
import java.io.File

const val COMPLETIONS_LIMIT = 5

/**
 * Used as a response getter for a prefix entered from GUI
 */
@Service
class DSLAutoCompleter(filenameCompleterService: FilenameCompleterService) {
    private val filenameCompleter = filenameCompleterService.completer

    fun processFileQuery(tokens: List<String>, limit: Int): List<String> {
        return if (tokens.size > 2 && tokens[tokens.size - 3] == "location") {
            if (tokens.last().startsWith('"')) {
                completeFilename(tokens.last().drop(1), limit)
            } else emptyList()
        } else emptyList()
    }


    fun completeCommandPrefix(commandPrefix: String, limit: Int): List<String> {
        var scope: EntityType? = null
        var previousScope: EntityType? = null
        var inQuotes = false
        var isEscaped = false
        var parenthesisLevel = 0

        for (char in commandPrefix) {
            if (char == '"' && !isEscaped) {
                inQuotes = !inQuotes
            }
            if (!inQuotes) {
                when (char) {
                    '(' -> {
                        if (scope == null && previousScope != EntityType.File) {
                            scope = EntityType.File
                        } else parenthesisLevel++
                    }

                    ')' -> {
                        if (scope == EntityType.File) {
                            previousScope = EntityType.File
                            scope = null;
                        } else {
                            if (parenthesisLevel > 0) parenthesisLevel--
                            else return emptyList()
                        }
                    }

                    '[' -> {
                        if (scope == null && previousScope != EntityType.Relationship && previousScope != null) {
                            scope = EntityType.Relationship
                        } else return emptyList()
                    }

                    ']' -> {
                        if (scope == EntityType.Relationship) {
                            previousScope = EntityType.Relationship
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

        val lastOpeningIndex = commandPrefix.indexOfLast { it == '(' || it == '[' }
        if (lastOpeningIndex == -1) return emptyList()

        if (lastOpeningIndex + 1 > commandPrefix.length - 1) return emptyList()

        val lastScope = commandPrefix.substring(lastOpeningIndex + 1, commandPrefix.length)
        val tokens = splitIntoTokens(lastScope)

        if (commandPrefix[lastOpeningIndex] == '(') {
            val filenames = processFileQuery(tokens, limit)
            if (filenames.isEmpty()) return emptyList()
            if (tokens.isEmpty()) return filenames
            return filenames.map { it.drop(tokens.last().length - 1) }.map { commandPrefix + it + '"'}
        }

        return emptyList()
    }


    /**
     * Finds out the files beginning with the given prefix
     * @param filenamePrefix Looking for files with this prefix
     * @param limit return at most this many entries
     * @return At most [limit] files matching the given prefix
     */
    private fun completeFilename(filenamePrefix: String, limit: Int): List<String> =
        filenameCompleter.lookup(filenamePrefix, limit)
            .map { it.joinToString(prefix = File.separator, separator = File.separator) }

    /**
     * Predicts the DSL command with the next word
     * @param commandPrefix Prefix to find the current word continuation for
     * @param limit return at most this many entries
     * @return At most [limit] words matching the given prefix
     */
    fun complete(commandPrefix: String, limit: Int = COMPLETIONS_LIMIT): List<String> {
        val completionList = completeCommandPrefix(commandPrefix, limit)
        return completionList
    }
}