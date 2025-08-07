package com.graphle.graphlemanager.file

import org.springframework.stereotype.Service

@Service
class FileService(private val fileRepository: FileRepository, private val fileSweeper: FileSweeper) {
    init {
        fileSweeper.startSweeping()
    }

    fun filesFromFileByRelationship(fromLocation: String, relationshipName: String): List<String> =
        fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)
}