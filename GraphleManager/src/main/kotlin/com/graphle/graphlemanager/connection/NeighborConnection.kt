package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.File
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

data class NeighborConnection(
    @Id @GeneratedValue val id: UUID? = null,
    val relationship: String,
    val toFiles: List<File>
) {
    constructor(
        relationship: String,
        toFiles: List<File>
    ): this(
        id = null,
        relationship = relationship,
        toFiles = toFiles
    )
}
