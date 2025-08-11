package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path

@Service
class FileService(private val fileRepository: FileRepository, private val fileSweeper: Neo4JSweeper) {
    fun filesFromFileByRelationship(fromLocation: AbsolutePathString, relationshipName: String): List<AbsolutePathString> =
        fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)

    fun addFile(location: AbsolutePathString) {
        Files.createFile(Path(location))
    }

    fun removeFile(location: AbsolutePathString) {
        Files.deleteIfExists(Path(location))
    }
}