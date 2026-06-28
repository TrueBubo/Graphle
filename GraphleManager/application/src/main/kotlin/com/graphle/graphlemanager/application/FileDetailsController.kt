package com.graphle.graphlemanager.application

import com.graphle.graphlemanager.commons.AbsolutePathString
import com.graphle.graphlemanager.file.File
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.IOException

@Controller
class FileDetailsController(
    private val fileDetailsService: FileDetailsService,
) {
    /**
     * Finds the information about the file at [location].
     *
     * This is the application-level aggregation point for file details. It combines live filesystem
     * hierarchy, persisted custom connections, and persisted tags into the GraphQL File response.
     */
    @QueryMapping
    fun fileByLocation(@Argument location: AbsolutePathString, @Argument showHiddenFiles: Boolean = true): File? =
        fileDetailsService.fileByLocation(location, showHiddenFiles)

    @Suppress("unused")
    @GraphQlExceptionHandler
    private fun handleFileReadException(exception: IOException): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message("Could not read a file at a given location, please check permissions: ${exception.message}")
            .build()
    }
}
