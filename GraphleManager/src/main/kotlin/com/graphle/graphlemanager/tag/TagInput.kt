package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.tag.Tag
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import java.util.UUID

data class TagInput(
    @GeneratedValue @Id val id: UUID?,
    val name: String,
    val value: String?
) {
    constructor(name: String, value: String?) : this(
        null,
        name,
        value
    )
}

