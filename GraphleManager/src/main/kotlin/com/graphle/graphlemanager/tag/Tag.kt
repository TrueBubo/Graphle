package com.graphle.graphlemanager.tag

import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

/**
 * Represents a tag that can be associated with files
 * @property id Unique identifier for the tag in the database
 * @property _name Internal name field
 * @property value Optional value associated with the tag
 */
@ConsistentCopyVisibility
@kotlinx.serialization.Serializable
@Node
data class Tag private constructor(
    @Transient @GeneratedValue @Id val id: UUID? = null,
    @SerialName("name") private val _name: String?,
    val value: String?,
) {
    val name: String get() = _name!!

    /**
     * Public constructor for creating a Tag instance
     * @param name The name of the tag
     * @param value Optional value for the tag
     */
    constructor(name: String, value: String?) : this(
        null,
        name,
        value
    )
}
