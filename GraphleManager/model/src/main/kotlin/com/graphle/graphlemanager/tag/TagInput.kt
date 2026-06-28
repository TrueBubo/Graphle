package com.graphle.graphlemanager.tag

/**
 * Input data class for creating or removing tags
 * @property name The name of the tag
 * @property value Optional value for the tag
 */
data class TagInput(
    val name: String,
    val value: String?
) {}

