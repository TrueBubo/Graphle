package com.graphle.graphlemanager.Tag

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import java.util.*

data class Tag(
    @GeneratedValue @Id val id: UUID?,
    val name: String,
    val value: String?,
    val numericValue: Double?
) {
    constructor(name: String, value: String?, numericValue: Double?) : this(
        null,
        name,
        value,
        numericValue
    )
}
