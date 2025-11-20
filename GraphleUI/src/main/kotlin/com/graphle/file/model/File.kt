package com.graphle.file.model

import com.graphle.tag.model.Tag
import kotlinx.serialization.Serializable

/**
 * Represents a file with its associated tags and connections.
 *
 * @property location Absolute path to the file
 * @property tags List of tags attached to this file
 * @property connections List of relationships to other files
 */
@Serializable
data class File(
    val location: String,
    val tags: List<Tag>,
    val connections: List<Connection>
)