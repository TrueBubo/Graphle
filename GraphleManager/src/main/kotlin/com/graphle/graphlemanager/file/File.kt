package com.graphle.graphlemanager.file

import com.graphle.graphlemanager.connections.RangedNeighborConnections
import com.graphle.graphlemanager.tag.Tag
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

data class File(
    @Id @GeneratedValue val id: UUID? = null,
    val location: String,
    val updated: Long,
    val tags: List<Tag>,
    val connections: RangedNeighborConnections?
) {
    constructor(
        location: String,
        updated: Long,
        tags: List<Tag>,
        connections: RangedNeighborConnections?
    ) : this(
        id = null,
        location = location,
        updated = updated,
        tags = tags,
        connections = connections
    )
}


