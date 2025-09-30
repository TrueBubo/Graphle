package com.graphle.graphlemanager.init

import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service

// Setting up Neo4J for better query performance
@Service
class Neo4JConfig(neo4jClient: Neo4jClient) {
    init {
        val fileLocationIndex = "CREATE INDEX file_location_index IF NOT EXISTS FOR (f:File) ON (f.location);"
        neo4jClient.query(fileLocationIndex)
            .run()

        val tagNameIndex = "CREATE INDEX tag_name IF NOT EXISTS FOR (t:Tag) ON (t.name)"
        neo4jClient.query(tagNameIndex)
            .run()
    }
}