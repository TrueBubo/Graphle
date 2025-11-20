package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.file.File
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

/**
 * Stores a collection of files with a given relationship
 * @param relationship The name of the relationship in DB
 * @param to File connected via that relationship from a given point
 */
data class NeighborConnection(
    @Id @GeneratedValue val id: UUID? = null,
    val relationship: String,
    val to: AbsolutePathString,
    val value: String? = null
)