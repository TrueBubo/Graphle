package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString

/**
 * Represents a relationship connection between two files in the file system
 * @property name The name of the relationship
 * @property value Optional value associated with the relationship
 * @property from Absolute path of the source file
 * @property to Absolute path of the destination file
 * @property bidirectional Whether the relationship is bidirectional
 */
@kotlinx.serialization.Serializable
data class Connection(
    val name: String,
    val value: String? = null,
    val from: AbsolutePathString,
    val to: AbsolutePathString,
    val bidirectional: Boolean? = null
)