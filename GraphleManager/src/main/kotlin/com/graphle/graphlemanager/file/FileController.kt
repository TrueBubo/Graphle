package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.ConnectionController
import com.graphle.graphlemanager.tag.TagController
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path
import java.nio.file.FileAlreadyExistsException

private class FileWriteException(message: String) : RuntimeException(message)


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
        return if (Files.exists(Path(location))) {
            File(
                location,
                tagController.tagsByFileLocation(location),
                connectionController.neighborsByFileLocation(location)
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
    ): List<AbsolutePathString> =
        fileService.filesFromFileByRelationship(fromLocation, relationshipName)

    @GraphQlExceptionHandler
    private fun handleFileWriteException(ex: FileWriteException): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message("Could not write to a file at a given location, please check permissions or whether the parents exist")
            .build()
    }

    @GraphQlExceptionHandler
    private fun handleFileWriteException(ex: FileAlreadyExistsException): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message("Could not create a file, it already exists")
            .build()
    }

    @MutationMapping
    fun addFile(@Argument location: AbsolutePathString): File {
        fileService.addFile(location)
        return File(location, listOf(), listOf())
    }

    @MutationMapping
    fun removeFile(@Argument location: AbsolutePathString): String {
        fileService.removeFile(location)
        return location
    }

    @MutationMapping
    fun moveFile(@Argument locationFrom: AbsolutePathString, @Argument locationTo: AbsolutePathString): MoveFileResponse {
        fileService.moveFile(locationFrom, locationTo)
        return MoveFileResponse(locationFrom, locationTo)
    }
}
