package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.dsl.FilenameCompleterService
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import org.apache.commons.io.FileUtils

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val filenameCompleterService: FilenameCompleterService
) {
    fun filesFromFileByRelationship(
        fromLocation: AbsolutePathString,
        relationshipName: String
    ): List<Connection> =
        when (relationshipName) {
            "descendant" -> descendantsOfFile(fromLocation).map {
                Connection(
                    name = "descendant",
                    value = null,
                    from = fromLocation,
                    to = it,
                )
            }
            "parent" -> (parentOfFile(fromLocation)?.let { listOf(it) } ?: emptyList()).map {
                Connection(
                    name = "parent",
                    value = null,
                    from = fromLocation,
                    to = it,
                )
            }
            else -> fileRepository.getFileLocationsByConnections(fromLocation, relationshipName).map {
                Connection(
                    name = relationshipName,
                    value = null,
                    from = fromLocation,
                    to = it,
                )
            }
        }

    fun fileType(location: String): FileType? {
        val file = File(location)
        if (!file.exists()) return null
        return if (file.isDirectory) FileType.Directory else FileType.File
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
        removeFileByLocationAction: (AbsolutePathString) -> Unit = {
            val path = Path(it)
            if (path.toFile().isDirectory) FileUtils.deleteDirectory(path.toFile())
            else Files.deleteIfExists(path)
        }
    ) {
        removeFileByLocationAction(location)
        fileRepository.removeFileByLocation(location)
    }

    fun moveFile(
        fromLocation: AbsolutePathString,
        toLocation: AbsolutePathString,
        moveFileAction: (AbsolutePathString, AbsolutePathString) -> Unit = { from, to ->
            Files.move(Path(from), Path(to))
        }
    ) {
        try {
            println("Before moving file from $fromLocation to $toLocation")
            moveFileAction(fromLocation, toLocation)
            println("After moving file from $fromLocation to $toLocation")
            fileRepository.moveFile(fromLocation, toLocation)
        } catch (e: Exception) {
            System.err.println("Cannot move file from $fromLocation to ${toLocation}: $e")
        }
    }
}