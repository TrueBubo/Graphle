package com.graphle.graphlemanager.tag

/**
 * Flattened representation of a tag for a file, used for database queries
 * @property location File location path
 * @property tagName Name of the tag
 * @property tagValue Optional value of the tag
 */
data class TagForFileFlattened(
    val location: String,
    val tagName: String?,
    val tagValue: String?
)
