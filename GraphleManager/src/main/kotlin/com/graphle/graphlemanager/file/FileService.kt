package com.graphle.graphlemanager.file

import org.springframework.stereotype.Service

@Service
class FileService(private val fileRepository: FileRepository, private val fileSweeper: FileSweeper) {
    init {
        fileSweeper.startSweeping()
    }

    fun filesFromFileByRelationship(fromLocation: AbsolutePathString, relationshipName: String): List<AbsolutePathString> =
        fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)
}