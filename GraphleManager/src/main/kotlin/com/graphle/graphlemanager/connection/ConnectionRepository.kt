package com.graphle.graphlemanager.connection

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConnectionRepository : Neo4jRepository<NeighborConnection, UUID> {
}