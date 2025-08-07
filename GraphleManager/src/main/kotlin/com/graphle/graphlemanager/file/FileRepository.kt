package com.graphle.graphlemanager.file

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FileRepository : Neo4jRepository<File, UUID> {
    @Query("MATCH (n:File {location: \$fileLocation})-[r]-(m) delete n,r")
    fun removeFileByLocation(fileLocation: String)
}
