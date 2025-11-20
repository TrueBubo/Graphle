package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.Connection
import com.graphle.graphlemanager.tag.Tag
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

typealias AbsolutePathString = String

/**
 * Stores information about the file on the filesystem
 * @param location The absolute path of the file
 * @param tags file is marked with these tags
 * @param connections How is the file related to other files
 */
@Serializable
@ConsistentCopyVisibility
data class File private constructor(
    @Transient @Id @GeneratedValue val id: UUID? = null,
    val location: AbsolutePathString,
    val tags: List<Tag>,
    val connections: List<Connection>
) {
    /**
     * Public constructor for creating a File instance
     * @param location The absolute path of the file
     * @param tags List of tags associated with the file
     * @param connections List of connections to other files
     */
    constructor(
        location: AbsolutePathString,
        tags: List<Tag>,
        connections: List<Connection>
    ) : this(
        id = null,
        location = location,
        tags = tags,
        connections = connections
    )
}

/**
 * Represents the type of a file system entity
 */
enum class FileType {
    File,
    Directory
}

