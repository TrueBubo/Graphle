package com.graphle.graphlemanager.file

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FileRepository : Neo4jRepository<File, UUID> {
    /**
     * Removes the file with given [fileLocation] including all the connection that went to it
     * @param fileLocation Absolute path of the file on the server
     */
    @Query("MATCH (n:File {location: \$fileLocation})-[r]-(m) DELETE n,r")
    fun removeFileByLocation(fileLocation: String)

    @Query("MATCH (:File {location: \$fromLocation})-[:Relationship {name: \$relationshipName}]-(toFile:File) RETURN toFile.location")
    fun getFileLocationsByConnections(fromLocation: String, relationshipName: String): List<String>
}
