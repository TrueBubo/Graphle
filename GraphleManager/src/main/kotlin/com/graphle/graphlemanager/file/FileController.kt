package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.ConnectionController
import com.graphle.graphlemanager.tag.TagController
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.lang.System.currentTimeMillis

@Controller
class FileController(
    private val fileService: FileService,
    private val tagController: TagController,
    private val connectionController: ConnectionController
) {
    @QueryMapping
    fun fileByLocation(@Argument location: String): File {
        return File(
            location,
            tagController.tagsByFileLocation(location),
            connectionController.neighborsByFileLocation(location)
        )
    }
}
