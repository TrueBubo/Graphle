package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path

@Service
class FileService(private val fileRepository: FileRepository, private val fileSweeper: Neo4JSweeper) {
    fun filesFromFileByRelationship(
        fromLocation: AbsolutePathString,
        relationshipName: String
    ): List<AbsolutePathString> =
        fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)

    fun addFile(
        location: AbsolutePathString,
        createFileAction: (AbsolutePathString) -> Unit = { Files.createFile(Path(it)) }
    ) {
        createFileAction(location)
    }

    fun removeFile(
        location: AbsolutePathString,
        removeFileByLocationAction: (AbsolutePathString) -> Unit = { Files.deleteIfExists(Path(it)) }
    ) {
        removeFileByLocationAction(location)
        fileRepository.removeFileByLocation(location)
    }

    fun moveFile(
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
        moveFileAction: (AbsolutePathString, AbsolutePathString) -> Unit = { from, to ->
            Files.move(Path(from), Path(to))
        }
    ) {
        moveFileAction(locationFrom, locationTo)
    }
}