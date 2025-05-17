package com.graphle.graphlemanager.tag

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagRepository : Neo4jRepository<Tag, UUID> {
    @Query("MATCH (f:File {location: \$fileLocation})-[:HasTag]->(t:Tag) RETURN t.name AS name, t.value AS value")
    fun tagsByFileLocation(fileLocation: String): List<Tag>
}
