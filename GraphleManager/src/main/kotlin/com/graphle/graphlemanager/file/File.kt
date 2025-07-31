package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connection.NeighborConnection
import com.graphle.graphlemanager.tag.Tag
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

/**
 * Stores information about the file on the filesystem
 * @param location The absolute path of the file
 * @param tags file is marked with these tags
 * @param connections How is the file related to other files
 */
data class File(
    @Id @GeneratedValue val id: UUID? = null,
    val location: String,
    val tags: List<Tag>,
    val connections: List<NeighborConnection>
) {
    constructor(
        location: String,
        tags: List<Tag>,
        connections: List<NeighborConnection>
    ) : this(
        id = null,
        location = location,
        tags = tags,
        connections = connections
    )
}


