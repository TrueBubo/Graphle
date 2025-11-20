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
    /**
     * Retrieves all neighboring files connected to the specified file location
     * @param fromLocation The source file location
     * @return List of neighbor connections with relationship details
     */
    @Query("MATCH (file:File {location: \$fromLocation})-[r:Relationship]->(neighbor:File) RETURN r.name AS relationship, r.value AS value, neighbor.location AS to")
    fun neighborsByFileLocation(fromLocation: AbsolutePathString): List<NeighborConnection>

    /**
     * Adds a connection without a value between two files
     * @param name The relationship name
     * @param locationFrom The source file location
     * @param locationTo The destination file location
     */
    @Query("MERGE (file1:File {location: \$locationFrom}) MERGE (file2:File {location: \$locationTo}) MERGE (file1)-[:Relationship {name:\$name}]->(file2)")
    fun addConnection(
        name: String,
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
    )

    /**
     * Adds a connection with a value between two files
     * @param name The relationship name
     * @param value The relationship value
     * @param locationFrom The source file location
     * @param locationTo The destination file location
     */
    @Query("MERGE (file1:File {location: \$locationFrom}) MERGE (file2:File {location: \$locationTo}) MERGE (file1)-[:Relationship {name: \$name, value: \$value}]->(file2)")
    fun addConnection(
        name: String,
        value: String,
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
    )

    /**
     * Removes a connection with a specific value between two files
     * @param locationFrom The source file location
     * @param locationTo The destination file location
     * @param name The relationship name
     * @param value The relationship value
     */
    @Query("MATCH (:File {location: \$locationFrom})-[r:Relationship {name: \$name, value: \$value}]->(:File {location: \$locationTo}) DELETE r")
    fun removeConnection(
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
        name: String,
        value: String
    )

    /**
     * Removes a connection without a value between two files
     * @param locationFrom The source file location
     * @param locationTo The destination file location
     * @param name The relationship name
     */
    @Query("MATCH (:File {location: \$locationFrom})-[r:Relationship]" +
            "->(:File {location: \$locationTo}) WHERE r.name = \$name AND r.value IS NULL DELETE r")
    fun removeConnection(
        locationFrom: AbsolutePathString,
        locationTo: AbsolutePathString,
        name: String,
    )
}