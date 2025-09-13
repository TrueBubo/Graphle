package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Neo4j repository for searching for relationships between files on the system
 */
@Repository
interface ConnectionRepository : Neo4jRepository<NeighborConnection, UUID> {
    @Query("MATCH (file:File {location: \$fromLocation})-[r:Relationship]->(neighbor:File) RETURN r.name AS relationship, r.value AS value, neighbor.location AS to")
    fun neighborsByFileLocation(fromLocation: AbsolutePathString): List<NeighborConnection>

    @Query("MERGE (file1:File {location: \$locationFrom}) MERGE (file2:File {location: \$locationTo}) MERGE (file1)-[:Relationship {name:\$name}]->(file2)")
    fun addConnection(
        name: String,
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
    )

    @Query("MERGE (file1:File {location: \$locationFrom}) MERGE (file2:File {location: \$locationTo}) MERGE (file1)-[:Relationship {name: \$name, value: \$value}]->(file2)")
    fun addConnection(
        name: String,
        value: String,
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
    )
}