package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service
import java.io.File

const val COMPLETIONS_LIMIT = 5

/**
 * Used as a response getter for a prefix entered from GUI
 */
@Service
class DSLAutoCompleter(filenameCompleterService: FilenameCompleterService) {
    private val filenameCompleter = filenameCompleterService.completer

    companion object {
        private enum class TokenType {
            VARIABLE_NAME,
            OPERATOR,
            VALUE,
            CONJUNCTION
        }

        private enum class EntityType {
            File,
            Relationship
        }


        fun splitIntoTokens(text: String): List<String>  = buildList {
            var inQuotes = false
            var isEscaped = false
            val word = StringBuilder()
            println(text)
            for (char in text) {
                if (char == '"' && !isEscaped) {
                    inQuotes = !inQuotes
                    if (!inQuotes) {
                        add(word.toString())
                        word.clear()
                    } else {
                        word.append(char)
                    }
                    continue

                }
                val wasEscaped = isEscaped
                isEscaped = false
                if (char == '\\' && wasEscaped) {
                    word.append('\\')
                    continue
                } else if (char == '\\'){
                    isEscaped = true
                    continue
                }

                if (!inQuotes && char == ' ' && word.isEmpty()) {
                    continue
                } else if (!inQuotes && char == ' ') {
                    add(word.toString())
                    word.clear()
                    continue
                }

                word.append(char)
            }
            if (word.isNotEmpty()) add(word.toString())
        }


    }

    fun processFileQuery(tokens: List<String>, limit: Int): List<String> {
        return if (tokens.size > 2 && tokens[tokens.size - 3] == "location") {
            completeFilename(tokens.last(), limit)
        } else emptyList()
    }


    fun completeCommandPrefix(commandPrefix: String, limit: Int): List<String> {
        var scope: EntityType? = null
        var previousScope: EntityType? = null
        var inQuotes = false
        var isEscaped = false

        for (char in commandPrefix) {
            if (char == '"' && !isEscaped) {
                inQuotes = !inQuotes
            }
            if (!inQuotes) {
                when (char) {
                    '(' -> {
                        if (scope == null && previousScope != EntityType.File) {
                            scope = EntityType.File
                        } else return emptyList()
                    }

                    ')' -> {
                        if (scope == EntityType.File) {
                            previousScope = EntityType.File
                            scope = null;
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

        if (commandPrefix[lastOpeningIndex] == '(') return processFileQuery(tokens, limit)

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