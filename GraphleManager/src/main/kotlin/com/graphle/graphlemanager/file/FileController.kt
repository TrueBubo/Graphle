package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connections.RangedNeighborConnections
import com.graphle.graphlemanager.tag.TagService
import com.graphle.graphlemanager.time.TimeRange
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.lang.System.currentTimeMillis

@Controller
class FileController(private val fileService: FileService, private val tagService: TagService) {
    @QueryMapping
    fun fileByLocation(@Argument location: String): File {
        return File(
            location,
            currentTimeMillis(),
            tagService.tagsForFileLocation(location),
            RangedNeighborConnections(
                TimeRange(to = currentTimeMillis()),
                listOf()
            )
        )
    }
}
