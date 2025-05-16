package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connections.RangedNeighborConnections
import com.graphle.graphlemanager.time.TimeRange
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class FileController(private val fileService: FileService) {
    @QueryMapping
    fun fileByLocation(@Argument location: String): File {
        return File(
            location,
            System.currentTimeMillis(),
            fileService.tagsForFileLocation(location),
            RangedNeighborConnections(
                TimeRange(0, System.currentTimeMillis()),
                listOf()
            )
        )
    }
}
