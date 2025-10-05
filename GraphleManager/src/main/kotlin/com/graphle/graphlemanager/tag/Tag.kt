package com.graphle.graphlemanager.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

@ConsistentCopyVisibility
@kotlinx.serialization.Serializable
@Node
data class Tag private constructor(
    @Transient @GeneratedValue @Id val id: UUID? = null,
    @SerialName("name") private val _name: String?,
    val value: String?,
) {
    val name: String get() = _name!!
    constructor(name: String, value: String?) : this(
        null,
        name,
        value
    )
}
