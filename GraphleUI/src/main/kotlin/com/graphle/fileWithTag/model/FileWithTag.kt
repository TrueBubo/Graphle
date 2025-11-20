package com.graphle.fileWithTag.model

import com.graphle.tag.model.Tag
import kotlinx.serialization.Serializable

/**
 * Represents a file associated with a specific tag.
 *
 * @property location File path
 * @property tag Associated tag
 */
@Serializable
data class FileWithTag(
    val location: String,
    val tag: Tag
)