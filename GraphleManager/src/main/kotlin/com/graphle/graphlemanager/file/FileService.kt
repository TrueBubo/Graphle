package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.stereotype.Service

@Service
class FileService(private val fileRepository: FileRepository, private val fileSweeper: Neo4JSweeper) {
    fun filesFromFileByRelationship(fromLocation: AbsolutePathString, relationshipName: String): List<AbsolutePathString> =
        fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)
}