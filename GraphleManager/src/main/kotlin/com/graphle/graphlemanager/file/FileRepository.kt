package com.graphle.graphlemanager.file

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Neo4j repository for managing file nodes and their relationships in the database
 */
@Repository
interface FileRepository : Neo4jRepository<File, UUID> {
    /**
     * Removes the file with given [fileLocation] including all the connection that went to it
     * @param fileLocation Absolute path of the file on the server
     */
    @Query("MATCH (file:File {location: \$fileLocation}) DETACH DELETE file")
    fun removeFileByLocation(fileLocation: AbsolutePathString)

    /**
     * Retrieves all file locations connected to the specified file by the given relationship
     * @param fromLocation Source file location
     * @param relationshipName Name of the relationship to query
     * @return List of connected file locations
     */
    @Query("MATCH (:File {location: \$fromLocation})-[:Relationship {name: \$relationshipName}]-(toFile:File) RETURN toFile.location")
    fun getFileLocationsByConnections(fromLocation: AbsolutePathString, relationshipName: String): List<AbsolutePathString>

    /**
     * Updates the location of a file node in the database
     * @param locationFrom Current file location
     * @param locationTo New file location
     */
    @Query("MATCH (f:File {location: \$locationFrom}) SET f.location = \$locationTo")
    fun moveFile(locationFrom: AbsolutePathString, locationTo: AbsolutePathString)

    /**
     * Creates or updates a file node in the database
     * @param location Absolute path of the file
     */
    @Query("MERGE (:File {location: \$location})")
    fun addFileNode(location: AbsolutePathString)

}
