package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.dsl.FilenameCompleterService
import io.micrometer.common.util.StringUtils.isNotEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import org.apache.commons.io.FileUtils

/**
 * Service for managing file operations on the filesystem and database
 * @param fileRepository Repository for database operations on files
 * @param filenameCompleterService Service for maintaining file path autocomplete functionality
 */
@Service
class FileService(
    private val fileRepository: FileRepository,
    private val filenameCompleterService: FilenameCompleterService
) {
    private val supervisorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Retrieves all files connected to the specified file by the given relationship
     * @param fromLocation The source file location
     * @param relationshipName The name of the relationship to query
     * @return List of connections matching the relationship
     */
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


    /**
     * Creates a new file at the specified location
     * @param location Absolute path where the file should be created
     * @param createFileAction Function to perform the actual file creation (default: Files.createFile)
     */
    fun addFile(
        location: AbsolutePathString,
        createFileAction: (AbsolutePathString) -> Unit = { Files.createFile(Path(it)) }
    ) {
        insertFilesToCompleter(listOf(location))
        createFileAction(location)
    }

    /**
     * Adds a file node to the database if the file exists
     * @param location Absolute path of the file
     */
    fun addFileNode(location: AbsolutePathString) {
        if (Files.exists(Path(location))) {
            fileRepository.addFileNode(location)
        }
    }

    /**
     * Removes a file from the filesystem and database
     * @param location Absolute path of the file to remove
     * @param removeFileByLocationAction Function to perform the actual file deletion
     */
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

    /**
     * Moves a file from one location to another and updates the database
     * @param fromLocation The source file location
     * @param toLocation The destination file location
     * @param moveFileAction Function to perform the actual file move operation
     */
    fun moveFile(
        fromLocation: AbsolutePathString,
        toLocation: AbsolutePathString,
        moveFileAction: (AbsolutePathString, AbsolutePathString) -> Unit = { from, to ->
            Files.move(Path(from), Path(to))
        }
    ) {
        try {
            moveFileAction(fromLocation, toLocation)
            fileRepository.moveFile(fromLocation, toLocation)
        } catch (e: Exception) {
            System.err.println("Cannot move file from $fromLocation to ${toLocation}: $e")
        }
    }

    /**
     * Inserts file paths into the autocomplete service for DSL command completion
     * @param locations List of file paths to add to the completer
     */
    fun insertFilesToCompleter(locations: List<AbsolutePathString>) {
        supervisorScope.launch {
            locations.forEach { location ->
                filenameCompleterService.completer.insert(
                    location.split(File.separator).filter(::isNotEmpty)
                )
            }
        }
    }

    companion object {
        /**
         * Determines the file type at the given location
         * @param location Absolute path to check
         * @return FileType if exists, null otherwise
         */
        fun fileType(location: String): FileType? {
            val file = File(location)
            if (!file.exists()) return null
            return if (file.isDirectory) FileType.Directory else FileType.File
        }

        /**
         * Retrieves all direct descendants (children) of the specified file/directory
         * @param fromLocation The parent directory location
         * @param getDescendantsAction Function to retrieve descendants
         * @return List of absolute paths of descendant files
         */
        fun descendantsOfFile(
            fromLocation: AbsolutePathString,
            getDescendantsAction: (AbsolutePathString) -> List<AbsolutePathString> = { filename ->
                val path = Path(filename)
                if (path.isDirectory()) Files.list(path).toList().map { it.absolutePathString() } else emptyList()
            }
        ): List<AbsolutePathString> = getDescendantsAction(fromLocation)

        /**
         * Retrieves the parent directory of the specified file
         * @param fromLocation The file location
         * @param getParentAction Function to retrieve the parent
         * @return Absolute path of parent directory, or null if at root
         */
        fun parentOfFile(
            fromLocation: AbsolutePathString,
            getParentAction: (AbsolutePathString) -> AbsolutePathString? = { Path(fromLocation).toFile().parent }
        ): AbsolutePathString? = getParentAction(fromLocation)
    }
}