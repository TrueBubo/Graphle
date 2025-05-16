package com.graphle.graphlemanager.File

import com.graphle.graphlemanager.Connections.NeighborConnection
import com.graphle.graphlemanager.Connections.RangedNeighborConnections
import com.graphle.graphlemanager.Time.TimeRange
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.lang.NonNull
import org.springframework.stereotype.Controller
import java.time.Instant

@Controller
class FileController(@param:NonNull private val fileService: FileService) {
    @QueryMapping
    fun fileByLocation(@Argument location: String): File {
        return File(
            location,
            System.currentTimeMillis(),
            fileService.tagsForFileLocation(location),
            RangedNeighborConnections(
                TimeRange(0, System.currentTimeMillis()),
                listOf<NeighborConnection>()
            )
        )
    }
}
