package com.graphle.graphlemanager.Tag

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagRepository : Neo4jRepository<Tag?, UUID?>
