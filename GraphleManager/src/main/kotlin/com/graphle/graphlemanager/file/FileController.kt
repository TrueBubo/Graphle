package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.connection.ConnectionController
import com.graphle.graphlemanager.connection.NeighborConnection
import com.graphle.graphlemanager.tag.TagController
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * GraphQL controller for managing information related to files
 * @param fileService Service used for retrieving required information related to files on the file system
 * @param tagController Controller used for querying information about the tags related to files
 * @param connectionController Controller used for querying information about the connection related to files
 * */
@Controller
class FileController(
    private val fileService: FileService,
    private val tagController: TagController,
    private val connectionController: ConnectionController
) {
    /**
     * Finds the information about the file at [location]
     * Located at server due to checking whether the file actually exists,
     * which the client could get false positive if it was not swept yet
     * @param location Absolute path in the file system
     * @return File information if the file exists or null
     */
    @QueryMapping
    fun fileByLocation(@Argument location: AbsolutePathString): File? {
        val descendentsConnection = NeighborConnection(
            "descendant",
            fileService.descendantsOfFile(location)
                .map { File(it, emptyList(), emptyList()) })
        val parentConnection = fileService.parentOfFile(location)
            ?.let { File(it, emptyList(), emptyList()) }
            ?.let { NeighborConnection("parent", listOf(it)) }
        val hierarchyNeighbors = buildList {
            add(descendentsConnection)
            parentConnection?.let { add(it) }
        }

        println("Called $hierarchyNeighbors")

        return if (Files.exists(Path(location))) {
            File(
                location,
                tagController.tagsByFileLocation(location),
                hierarchyNeighbors + connectionController.neighborsByFileLocation(location)
            )
        } else null
    }

    /**
     * Finds file locations related to [fromLocation] file via the relationship named [relationshipName]
     * @param fromLocation Absolute path of the file on the server
     * @param relationshipName  Name of the relationship between two files
     * @return List of absolute paths of files related to [fromLocation] via [relationshipName]
     * */
    @QueryMapping
    fun filesFromFileByRelationship(
        @Argument fromLocation: AbsolutePathString,
        @Argument relationshipName: String
    ): List<Connection> =
        fileService.filesFromFileByRelationship(fromLocation, relationshipName)

    /**
     * GraphQL client will return this error if there was an error with writing the file
     * @param exception Caught exception
     * @return error with the message
     */
    @GraphQlExceptionHandler
    private fun handleFileWriteException(exception: IOException): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message("Could not write to a file at a given location, please check permissions or whether the parents exist: ${exception.message}")
            .build()
    }

    /**
     * GraphQL client will return this error if the file already exists
     * @param exception Caught exception
     * @return error with the message
     */
    @GraphQlExceptionHandler
    private fun handleFileWriteException(exception: FileAlreadyExistsException): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message("Could not create a file, it already exists")
            .build()
    }

    /**
     * Adds a file to the file system at [location]
     * @param location Absolute path of the file to be created
     * @throws FileAlreadyExistsException If the file already exists on the file system
     * @throws java.io.IOException If the write could not happen
     * @return file created
     */
    @MutationMapping
    fun addFile(@Argument location: AbsolutePathString): File {
        fileService.addFile(location)
        return File(location, listOf(), listOf())
    }

    /**
     * Removes the file at [location] from file system, and removes it from database
     * @param location Absolute path of the file deleted
     * @throws IOException if the file could not be deleted
     * @return [location]
     */
    @MutationMapping
    fun removeFile(@Argument location: AbsolutePathString): AbsolutePathString {
        fileService.removeFile(location)
        return location
    }

    /**
     * Moves the file from [locationFrom] to [locationTo], and updates the info in database
     * @param locationFrom The original absolute path of the file
     * @param locationTo The new absolute path of the file
     * @return Information about the move
     */
    @MutationMapping
    fun moveFile(
        @Argument locationFrom: AbsolutePathString,
        @Argument locationTo: AbsolutePathString
    ): MoveFileResponse {
        fileService.moveFile(locationFrom, locationTo)
        return MoveFileResponse(locationFrom, locationTo)
    }
}
