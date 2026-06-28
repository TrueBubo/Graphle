package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.commons.AbsolutePathString
import com.graphle.graphlemanager.commons.normalize
import com.graphle.graphlemanager.connection.Connection
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.IOException
import java.nio.file.FileAlreadyExistsException

/**
 * GraphQL controller for managing information related to files
 * @param fileService Service used for retrieving required information related to files on the file system
 * */
@Controller
class FileController(
    private val fileService: FileService,
) {
    /**
     * Determines the type of file at the specified location
     * @param location Absolute path to check
     * @return FileType if the file exists, null otherwise
     */
    @QueryMapping
    fun fileType(@Argument location: AbsolutePathString): FileType? = FileService.fileType(location.normalize())

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
        fileService.filesFromFileByRelationship(fromLocation.normalize(), relationshipName)
            .also { connections -> fileService.insertFilesToCompleter(connections.map { it.to }) }

    /**
     * GraphQL client will return this error if there was an error with writing the file
     * @param exception Caught exception
     * @return error with the message
     */
    @Suppress("unused")
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
    @Suppress("unused")
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
        fileService.addFile(location.normalize())
        return File(location.normalize(), listOf(), listOf())
    }

    /**
     * Removes the file at [location] from file system, and removes it from database
     * @param location Absolute path of the file deleted
     * @throws IOException if the file could not be deleted
     * @return [location]
     */
    @MutationMapping
    fun removeFile(@Argument location: AbsolutePathString): AbsolutePathString {
        fileService.removeFile(location.normalize())
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
        fileService.moveFile(locationFrom.normalize(), locationTo.normalize())
        return MoveFileResponse(locationFrom.normalize(), locationTo.normalize())
    }
}
