package com.graphle.graphlemanager.connection

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Neo4j repository for searching for relationships between files on the system
 */
@Repository
interface ConnectionRepository : Neo4jRepository<NeighborConnection, UUID>