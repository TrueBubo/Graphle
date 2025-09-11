package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.dsl.FilenameCompleterService
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

@Service
class FileService(private val fileRepository: FileRepository, private val filenameCompleterService: FilenameCompleterService) {
    fun filesFromFileByRelationship(
        fromLocation: AbsolutePathString,
        relationshipName: String
    ): List<AbsolutePathString> =
        when (relationshipName) {
            "descendant" -> descendantsOfFile(fromLocation)
            "parent" -> parentOfFile(fromLocation)?.let { listOf(it) } ?: emptyList()
            else -> fileRepository.getFileLocationsByConnections(fromLocation, relationshipName)
        }

    fun descendantsOfFile(
        fromLocation: AbsolutePathString,
        getDescendantsAction: (AbsolutePathString) -> List<AbsolutePathString> = { filename ->
            val path = Path(filename)
            if (path.isDirectory()) Files.list(path).toList().map { it.absolutePathString() } else emptyList()
        }
    ): List<AbsolutePathString> = getDescendantsAction(fromLocation)

    fun parentOfFile(
        fromLocation: AbsolutePathString,
        getParentAction: (AbsolutePathString) -> AbsolutePathString? = { Path(fromLocation).toFile().parent }
    ): AbsolutePathString? = getParentAction(fromLocation)

    fun addFile(
        location: AbsolutePathString,
        createFileAction: (AbsolutePathString) -> Unit = { Files.createFile(Path(it)) }
    ) {
        filenameCompleterService.completer.insert(location.split(File.separator))
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