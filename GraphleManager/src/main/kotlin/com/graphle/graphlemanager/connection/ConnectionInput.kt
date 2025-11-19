package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.file.AbsolutePathString

/**
 * Input data class for creating or removing connections between files
 * @property name The name of the relationship
 * @property value Optional value associated with the relationship
 * @property from Absolute path of the source file
 * @property to Absolute path of the destination file
 * @property bidirectional Whether the relationship should be bidirectional
 */
data class ConnectionInput(
    val name: String,
    val value: String?,
    val from: AbsolutePathString,
    val to: AbsolutePathString,
    val bidirectional: Boolean
)
