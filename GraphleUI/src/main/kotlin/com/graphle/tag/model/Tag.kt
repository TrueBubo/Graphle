package com.graphle.tag.model

import com.graphle.common.addTagToFile
import com.graphle.common.apolloClient
import kotlinx.serialization.Serializable

/**
 * Represents a tag that can be attached to a file.
 *
 * @property name Tag name
 * @property value Optional tag value
 */
@Serializable
data class Tag(
    val name: String,
    val value: String?
) {
    /**
     * Saves this tag to a file on the server.
     *
     * @param location Path to the file to tag
     */
    suspend fun save(location: String) {
        if (value != null) apolloClient.addTagToFile(
            location,
            name,
            value
        )
        else apolloClient.addTagToFile(location, name)
    }
}