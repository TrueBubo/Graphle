package com.graphle.file.model

import com.graphle.common.addRelationshipToFile
import com.graphle.common.apolloClient
import kotlinx.serialization.Serializable

/**
 * Represents a relationship/connection between two files.
 *
 * @property from Source file path
 * @property to Target file path
 * @property name Name of the relationship type
 * @property value Optional value associated with the relationship
 */
@Serializable
data class Connection(
    val from: String,
    val to: String,
    val name: String,
    val value: String? = null
) {
    /**
     * Saves this connection to the server.
     *
     * @param bidirectional Whether to create a bidirectional relationship
     */
    suspend fun save(bidirectional: Boolean) {
        if (value != null) apolloClient.addRelationshipToFile(
            from = from,
            to = to,
            name = name,
            value = value,
            bidirectional = bidirectional
        ) else apolloClient.addRelationshipToFile(from = from, to = to, name = name, bidirectional = bidirectional)
    }
}