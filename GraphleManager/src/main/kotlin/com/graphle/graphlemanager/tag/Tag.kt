package com.graphle.graphlemanager.tag

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.*

data class Tag(
    @GeneratedValue @Id val id: UUID?,
    val name: String,
    val value: String?,
) {
    constructor(name: String, value: String?) : this(
        null,
        name,
        value
    )
}
