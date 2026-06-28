package com.graphle.graphlemanager.application

import com.graphle.graphlemanager.commons.AbsolutePathString
import com.graphle.graphlemanager.commons.normalize
import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.connection.ConnectionService
import com.graphle.graphlemanager.file.File
import com.graphle.graphlemanager.file.FileService
import com.graphle.graphlemanager.tag.TagService
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path

@Service
class FileDetailsService(
    private val fileService: FileService,
    private val tagService: TagService,
    private val connectionService: ConnectionService,
) {
    fun fileByLocation(location: AbsolutePathString, showHiddenFiles: Boolean = true): File? {
        val normalizedLocation = location.normalize()
        if (!Files.exists(Path(normalizedLocation))) return null

        val descendants = FileService.descendantsOfFile(normalizedLocation)
            .map {
                Connection(
                    name = "descendant",
                    value = null,
                    from = normalizedLocation,
                    to = it,
                )
            }
        val parentConnection = FileService.parentOfFile(normalizedLocation)
            ?.let {
                Connection(
                    name = "parent",
                    value = null,
                    from = normalizedLocation,
                    to = it,
                )
            }

        val hierarchyNeighbors = buildList {
            addAll(descendants)
            parentConnection?.let { add(it) }
        }

        val customConnections = connectionService.neighborsByFileLocation(normalizedLocation)
            .map {
                Connection(
                    name = it.relationship,
                    value = it.value,
                    from = normalizedLocation,
                    to = it.to,
                )
            }

        val connections = (hierarchyNeighbors + customConnections)
            .filter { Files.exists(Path(it.to)) && (showHiddenFiles || !Files.isHidden(Path(it.to))) }

        return File(
            location = normalizedLocation,
            tags = tagService.tagsByFileLocation(normalizedLocation),
            connections = connections
                .also { fileService.insertFilesToCompleter(connections.map { it.to }) },
        )
    }
}
