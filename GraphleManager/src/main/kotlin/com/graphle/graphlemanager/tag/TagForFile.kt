package com.graphle.graphlemanager.tag

import com.graphle.graphlemanager.file.AbsolutePathString
import kotlinx.serialization.Serializable

/**
 * Represents a file location with its associated tag
 * @property location Absolute path of the file
 * @property tag The tag associated with the file
 */
@Serializable
data class TagForFile(
    val location: AbsolutePathString,
    val tag: Tag,
)
