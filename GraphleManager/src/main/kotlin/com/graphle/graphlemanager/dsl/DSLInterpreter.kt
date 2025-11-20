package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.dsl.DSLUtil.splitIntoTokens
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.File
import com.graphle.graphlemanager.file.FileController
import com.graphle.graphlemanager.tag.TagForFile
import com.graphle.graphlemanager.tag.TagInput
import com.graphle.graphlemanager.tag.TagService
import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Represents a scope in a DSL query.
 * @property entityType The type of entity (File or Relationship)
 * @property text The text content within the scope
 */
data class Scope(val entityType: EntityType, val text: String)

const val FILE_SCOPE_OPENING = '('
const val FILE_SCOPE_CLOSING = ')'
const val RELATIONSHIP_SCOPE_OPENING = '['
const val RELATIONSHIP_SCOPE_CLOSING = ']'

const val HIGHER_PRIORITY_OPENING_TOKEN = "("
const val HIGHER_PRIORITY_CLOSING_TOKEN = ")"

/**
 * Marker interface for scope output results.
 */
sealed interface ScopeOutput

/**
 * Represents a filename as a scope output result.
 * @property path The absolute path to the file
 */
@JvmInline
value class Filename(val path: AbsolutePathString) : ScopeOutput

/**
 * Types of responses that can be returned from DSL command execution.
 */
enum class ResponseType {
    /** Error occurred during execution */
    ERROR,

    /** Operation completed successfully */
    SUCCESS,

    /** Response contains a list of filenames */
    FILENAMES,

    /** Response contains a list of connections */
    CONNECTIONS,

    /** Response contains file details */
    FILE,

    /** Response contains tag information */
    TAG
}

/**
 * Response object for DSL command execution.
 * @property type The type of response
 * @property responseObject List of response data as strings
 */
@Serializable
data class DSLResponse(val type: ResponseType, val responseObject: List<String>)

/**
 * Input for tag modification operations.
 * @property location The file location to modify tags for
 * @property tag The tag information to add or remove
 */
data class TagModificationInput(val location: AbsolutePathString, val tag: TagInput)

/**
 * Available DSL commands.
 * @property command The string representation of the command
 */
enum class Commands(val command: String) {
    /** Find files matching criteria */
    FIND("find"),

    /** Add a relationship between files */
    ADD_REL("addRel"),

    /** Remove a relationship between files */
    REMOVE_REL("removeRel"),

    /** Add a tag to a file */
    ADD_TAG("addTag"),

    /** Remove a tag from a file */
    REMOVE_TAG("removeTag"),

    /** Get detailed information about a file */
    DETAIL("detail"),

    /** Query files by tag */
    TAG("tag")
}

/**
 * Service responsible for interpreting and executing DSL commands.
 * @param scopeParser Parser for splitting commands into scopes
 * @param tokenInterpreter Interpreter for parsing command tokens
 * @param commandExecutor Executor for running scope queries
 * @param connectionService Service for managing file relationships
 * @param tagService Service for managing file tags
 * @param fileController Controller for file-related operations
 */
@Service
class DSLInterpreter(
    private val scopeParser: DSLScopeParser,
    private val tokenInterpreter: DSLTokenInterpreter,
    private val commandExecutor: DSLCommandExecutor,
    private val connectionService: ConnectionService,
    private val tagService: TagService,
    private val fileController: FileController
) {

    /**
     * Interprets and executes a DSL command.
     * @param command The DSL command string to interpret
     * @return The response containing execution results or error information
     */
    fun interpret(command: String): DSLResponse {
        val tokens = splitIntoTokens(command)
        if (tokens.isEmpty()) return parseError(command)

        return try {
            when (tokens.first()) {
                Commands.FIND.command -> {
                    interpretFind(command.drop(tokens.first().length + 1))
                }

                Commands.ADD_REL.command -> {
                    val connectionInput = tokenInterpreter.parseRelationshipTokens(tokens.drop(1))
                        ?: return parseError(command)
                    connectionService.addConnection(connectionInput)
                    DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.REMOVE_REL.command -> {
                    val connectionInput = tokenInterpreter.parseRelationshipTokens(tokens.drop(1))
                        ?: return parseError(command)
                    connectionService.removeConnection(connectionInput)
                    DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.ADD_TAG.command -> {
                    val tagInput = tokenInterpreter.parseTagModificationTokens(tokens.drop(1))
                        ?: return parseError(command)
                    tagService.addTagToFile(tagInput.location, tagInput.tag)
                    DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.REMOVE_TAG.command -> {
                    val tagInput = tokenInterpreter.parseTagModificationTokens(tokens.drop(1))
                        ?: return parseError(command)
                    tagService.removeTag(tagInput.location, tagInput.tag)
                    DSLResponse(ResponseType.SUCCESS, listOf())
                }

                Commands.DETAIL.command -> {
                    val filename = tokenInterpreter.parseDetailTokens(tokens.drop(1))
                        ?: return parseError(command)
                    val detail: File = fileController.fileByLocation(filename) ?: return DSLResponse(
                        ResponseType.ERROR,
                        listOf("File $filename not found")
                    )
                    DSLResponse(ResponseType.FILE, listOf(Json.encodeToString(detail)))
                }

                Commands.TAG.command -> {
                    val tag = tokenInterpreter.parseGetTokens(tokens.drop(1))
                        ?: return parseError(command)
                    val tagLocations = tagService.filesByTag(tag)
                    DSLResponse(
                        type = ResponseType.TAG,
                        responseObject = tagLocations.map { Json.encodeToString<TagForFile>(it) }
                    )
                }

                else -> DSLResponse(ResponseType.ERROR, listOf("Unknown command ${tokens.first()}"))
            }
        } catch (e: Exception) {
            DSLResponse(ResponseType.ERROR, listOf("Error executing command: ${e.message}"))
        }
    }

    /**
     * Creates an error response for parse failures.
     * @param command The command that failed to parse
     * @return A DSLResponse with error type and message
     */
    private fun parseError(command: String) =
        DSLResponse(ResponseType.ERROR, listOf("Unable to parse $command"))

    /**
     * Interprets a 'find' command to search for files.
     * @param command The find command string (without the 'find' keyword)
     * @return The response containing matching filenames or connections
     */
    fun interpretFind(command: String): DSLResponse {
        val scopes = scopeParser.splitSearchIntoScopes(command)
        if (scopes.isEmpty()) return parseError(command)

        return commandExecutor.executeFindCommand(scopes)
    }
}