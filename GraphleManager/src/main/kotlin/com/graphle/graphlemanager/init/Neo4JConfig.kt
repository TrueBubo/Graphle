package com.graphle.graphlemanager.init

import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service

/**
 * Configuration service for setting up Neo4j database indexes.
 * Creates indexes on startup to improve query performance.
 * @param neo4jClient The Neo4j client used to execute index creation queries
 */
@Service
class Neo4JConfig(neo4jClient: Neo4jClient) {
    init {
        try {
            val fileLocationIndex = "CREATE INDEX file_location_index IF NOT EXISTS FOR (f:File) ON (f.location);"
            neo4jClient.query(fileLocationIndex)
                .run()
        } catch (e: Exception) {
            if (!e.message?.contains("equivalent index already exists", ignoreCase = true)!!) {
                throw e
            }
        }

        try {
            val tagNameIndex = "CREATE INDEX tag_name IF NOT EXISTS FOR (t:Tag) ON (t.name)"
            neo4jClient.query(tagNameIndex)
                .run()
        } catch (e: Exception) {
            if (!e.message?.contains("equivalent index already exists", ignoreCase = true)!!) {
                throw e
            }
        }
    }
}