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
class DSLAutoCompleter(
    filenameCompleterService: FilenameCompleterService,
) {
    private val filenameCompleter = filenameCompleterService.completer

    /**
     * Predicts the DSL command with the next word
     * @param commandPrefix Prefix to find the current word continuation for
     * @param limit return at most this many entries
     * @return At most [limit] words matching the given prefix
     */
    fun complete(commandPrefix: String, limit: Int = COMPLETIONS_LIMIT): List<String> =
        completeCommandPrefix(commandPrefix, limit)

    /**
     * Completes a command prefix by determining the command type and delegating to the appropriate completion method.
     * @param commandPrefix The command prefix to complete
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
    fun completeCommandPrefix(commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.isEmpty()) return emptyList()
        return when (tokens.first()) {
            Command.FIND.command -> completeFindCommand(
                commandPrefix = commandPrefix.drop(Command.FIND.command.length + 1),
                limit = limit
            )

            Command.ADD_REL.command -> completeConnectionCommand(
                commandType = Command.ADD_REL.command,
                commandPrefix = commandPrefix.drop(Command.ADD_REL.command.length),
                limit = limit
            )

            Command.REMOVE_REL.command -> completeConnectionCommand(
                commandType = Command.REMOVE_REL.command,
                commandPrefix = commandPrefix.drop(Command.REMOVE_REL.command.length),
                limit = limit
            )

            Command.ADD_TAG.command -> completeTagCommand(
                commandType = Command.ADD_TAG.command,
                commandPrefix = commandPrefix.drop(Command.ADD_TAG.command.length + 1),
                limit = limit
            )

            Command.REMOVE_TAG.command -> completeTagCommand(
                commandType = Command.REMOVE_TAG.command,
                commandPrefix = commandPrefix.drop(Command.REMOVE_TAG.command.length + 1),
                limit = limit
            )

            Command.DETAIL.command -> completeDetailCommand(
                commandPrefix.drop(Command.DETAIL.command.length + 1),
                limit
            )

            Command.TAG.command -> emptyList()

            Command.ADD_FILE.command -> completeFilenamesCommand(
                commandPrefix = commandPrefix.drop(Command.ADD_FILE.command.length + 1),
                command = Command.ADD_FILE,
                limit = limit
            )

            Command.REMOVE_FILE.command -> completeFilenamesCommand(
                commandPrefix = commandPrefix.drop(Command.REMOVE_FILE.command.length + 1),
                command = Command.REMOVE_FILE,
                limit = limit
            )

            Command.MOVE_FILE.command -> completeFilenamesCommand(
                commandPrefix = commandPrefix.drop(Command.MOVE_FILE.command.length + 1),
                command = Command.MOVE_FILE,
                limit = limit,
                count = 2
            )

            else -> emptyList()
        }
    }

    /**
     * Completes a 'find' command by analyzing scopes and providing filename suggestions.
     * @param commandPrefix The find command prefix (without the 'find' keyword)
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
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
            if (tokens.isEmpty()) return filenames.map { "${Command.FIND.command} $it" }
            return filenames
                .map {
                    addFilenameToCommand(
                        commandType = Command.FIND.command,
                        filename = it,
                        commandPrefix = commandPrefix,
                        filenamePrefixToken = tokens.last(),
                    )
                }
        }

        return emptyList()
    }

    /**
     * Completes a connection command (addRel/removeRel) by providing filename suggestions.
     * @param commandType The command type (addRel or removeRel)
     * @param commandPrefix The command prefix (without the command keyword)
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
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

    /**
     * Completes a tag command (addTag/removeTag) by providing filename suggestions.
     * @param commandType The command type (addTag or removeTag)
     * @param commandPrefix The command prefix (without the command keyword)
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
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

    /**
     * Completes a detail command by providing filename suggestions.
     * @param commandPrefix The command prefix (without the 'detail' keyword)
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
    private fun completeDetailCommand(commandPrefix: String, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.size != 1) return emptyList()
        return completeFilenamesForToken(tokens.last(), limit)
            .map {
                addFilenameToCommand(
                    commandType = Command.DETAIL.command,
                    filename = it,
                    commandPrefix = commandPrefix,
                    filenamePrefixToken = tokens.last(),
                )
            }
    }

    /**
     * Completes a filenames only expecting command by providing filename suggestions.
     * @param commandPrefix The command prefix (without the command keyword)
     * @param count How many
     * @param limit Maximum number of completions to return
     * @return List of completion suggestions
     */
    private fun completeFilenamesCommand(commandPrefix: String, count: Int = 1, command: Command, limit: Int): List<String> {
        val tokens = splitIntoTokens(commandPrefix)
        if (tokens.isEmpty() || tokens.size > count) return emptyList()
        return completeFilenamesForToken(tokens.last(), limit)
            .map {
                addFilenameToCommand(
                    commandType = command.command,
                    filename = it,
                    commandPrefix = commandPrefix,
                    filenamePrefixToken = tokens.last(),
                )
            }
    }

    /**
     * Completes filenames for a single token, handling quoted strings.
     * @param token The token to complete
     * @param limit Maximum number of completions to return
     * @return List of filename completions
     */
    private fun completeFilenamesForToken(token: String, limit: Int): List<String> =
        (if (token.startsWith('"')) {
            completeFilenames(token.drop(1), limit)
        } else completeFilenames(token, limit))


    /**
     * Processes a file query and returns filename completions if applicable.
     * @param tokens The tokens in the file scope
     * @param limit Maximum number of completions to return
     * @return List of filename completions or empty list
     */
    private fun processFileQuery(tokens: List<String>, limit: Int): List<String> {
        return if (tokens.size > 2 && tokens[tokens.size - 3] == "location") {
            completeFilenamesForToken(tokens.last(), limit)
        } else emptyList()
    }

    /**
     * Adds a completed filename to the command string.
     * @param commandType The type of command
     * @param filename The completed filename
     * @param filenamePrefixToken The original filename prefix token
     * @param commandPrefix The command prefix before completion
     * @return The complete command with the filename added
     */
    private fun addFilenameToCommand(
        commandType: String,
        filename: String,
        filenamePrefixToken: String,
        commandPrefix: String
    ): String {
        val dropLetters =
            if (filenamePrefixToken.startsWith("\"")) filenamePrefixToken.length - 1 else filenamePrefixToken.length
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