package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.ConnectionController
import com.graphle.graphlemanager.tag.TagController
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.nio.file.Files
import kotlin.io.path.Path

@Controller
class FileController(
    private val fileService: FileService,
    private val tagController: TagController,
    private val connectionController: ConnectionController
) {
    /**
     * Finds the information about the file at [location]
     * Located at server due to checking whether the file actually exists,
     * which the client could get false positive if it was not swept yet
     * @param location Absolute path in the file system
     * @return File information if the file exists or null
     */
    @QueryMapping
    fun fileByLocation(@Argument location: AbsolutePathString): File? {
        return if (Files.exists(Path(location))) {
            File(
                location,
                tagController.tagsByFileLocation(location),
                connectionController.neighborsByFileLocation(location)
            )
        } else null
    }

    /**
     * Finds file locations related to [fromLocation] file via the relationship named [relationshipName]
     * @param fromLocation Absolute path of the file on the server
     * @param relationshipName  Name of the relationship between two files
     * @return List of absolute paths of files related to [fromLocation] via [relationshipName]
     * */
    @QueryMapping
    fun filesFromFileByRelationship(@Argument fromLocation: AbsolutePathString, @Argument relationshipName: String): List<AbsolutePathString> =
        fileService.filesFromFileByRelationship(fromLocation, relationshipName)
}
