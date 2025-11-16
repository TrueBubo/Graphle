package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import org.springframework.stereotype.Service
import java.io.File
import kotlin.text.iterator

const val COMPLETIONS_LIMIT = 5

/**
 * Used as a response getter for a prefix entered from GUI
 */
@Service
class DSLAutoCompleter(filenameCompleterService: FilenameCompleterService) {
    private val filenameCompleter = filenameCompleterService.completer

    /**
     * Predicts the DSL command with the next word
     * @param commandPrefix Prefix to find the current word continuation for
     * @param limit return at most this many entries
     * @return At most [limit] words matching the given prefix
     */
    fun complete(commandPrefix: String, limit: Int = COMPLETIONS_LIMIT): List<String> =
        completeCommandPrefix(commandPrefix, limit)

    fun completeCommandPrefix(commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.isEmpty()) return emptyList()
        return when (tokens.first()) {
            Commands.FIND.command -> completeFindCommand(
                commandPrefix = commandPrefix.drop(Commands.FIND.command.length + 1),
                limit = limit
            )

            Commands.ADD_REL.command -> completeConnectionCommand(
                commandType = Commands.ADD_REL.command,
                commandPrefix = commandPrefix.drop(Commands.ADD_REL.command.length),
                limit = limit
            )

            Commands.REMOVE_REL.command -> completeConnectionCommand(
                commandType = Commands.REMOVE_REL.command,
                commandPrefix = commandPrefix.drop(Commands.REMOVE_REL.command.length),
                limit = limit
            )

            Commands.ADD_TAG.command -> completeTagCommand(
                commandType = Commands.ADD_TAG.command,
                commandPrefix = commandPrefix.drop(Commands.ADD_TAG.command.length + 1),
                limit = limit
            )

            Commands.REMOVE_TAG.command -> completeTagCommand(
                commandType = Commands.REMOVE_TAG.command,
                commandPrefix = commandPrefix.drop(Commands.REMOVE_TAG.command.length + 1),
                limit = limit
            )

            Commands.DETAIL.command -> completeDetailCommand(
                commandPrefix.drop(Commands.DETAIL.command.length + 1),
                limit
            )

            Commands.TAG.command -> emptyList()

            else -> emptyList()
        }
    }

    private fun completeFindCommand(commandPrefix: String, limit: Int): List<String> {
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
                            scope = null
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
            if (tokens.isEmpty()) return filenames.map { "${Commands.FIND.command} $it" }
            return filenames
                .map {
                    addFilenameToCommand(
                        commandType = Commands.FIND.command,
                        filename = it,
                        commandPrefix = commandPrefix,
                        filenamePrefixToken = tokens.last(),
                    )
                }
        }

        return emptyList()
    }

    private fun completeConnectionCommand(commandType: String, commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.size !in 1..2) return emptyList()
        return completeFilenamesForToken(tokens.last(), limit)
            .map {
                addFilenameToCommand(
                    commandType = commandType,
                    filename = it,
                    commandPrefix = commandPrefix,
                    filenamePrefixToken = tokens.last(),
                )
            }
    }

    private fun completeTagCommand(commandType: String, commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.size != 1) return emptyList()
        return completeFilenamesForToken(tokens.last(), limit)
            .map {
                addFilenameToCommand(
                    commandType = commandType,
                    filename = it,
                    commandPrefix = commandPrefix,
                    filenamePrefixToken = tokens.last(),
                )
            }
    }

    private fun completeDetailCommand(commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.size != 1) return emptyList()
        return completeFilenamesForToken(tokens.last(), limit)
            .map {
                addFilenameToCommand(
                    commandType = Commands.DETAIL.command,
                    filename = it,
                    commandPrefix = commandPrefix,
                    filenamePrefixToken = tokens.last(),
                )
            }
    }

    private fun completeFilenamesForToken(token: String, limit: Int): List<String> =
        (if (token.startsWith('"')) {
            completeFilenames(token.drop(1), limit)
        } else completeFilenames(token, limit))


    private fun processFileQuery(tokens: List<String>, limit: Int): List<String> {
        return if (tokens.size > 2 && tokens[tokens.size - 3] == "location") {
            completeFilenamesForToken(tokens.last(), limit)
        } else emptyList()
    }

    private fun addFilenameToCommand(
        commandType: String,
        filename: String,
        filenamePrefixToken: String,
        commandPrefix: String
    ): String {
        val dropLetters = if (filenamePrefixToken.startsWith("\"")) filenamePrefixToken.length - 1 else filenamePrefixToken.length
        val filenamePrefixUnquoted = filenamePrefixToken.drop(if (dropLetters == filenamePrefixToken.length) 0 else 1)
        return if (filename.startsWith(filenamePrefixUnquoted))
            "$commandType ${commandPrefix}${filename.drop(dropLetters)}"
        else {
            "$commandType ${commandPrefix.dropLast(dropLetters)}$filename"
        }
    }


    /**
     * Finds out the files beginning with the given prefix
     * @param filenamePrefix Looking for files with this prefix
     * @param limit return at most this many entries
     * @return At most [limit] files matching the given prefix
     */
    private fun completeFilenames(filenamePrefix: String, limit: Int): List<String> =
        filenameCompleter.lookup(filenamePrefix, limit)
            .map { it.joinToString(prefix = File.separator, separator = File.separator) }

}