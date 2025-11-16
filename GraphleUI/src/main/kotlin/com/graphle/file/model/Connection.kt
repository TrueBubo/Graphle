package com.graphle.file.model

import com.graphle.common.addRelationshipToFile
import com.graphle.common.apolloClient
import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val from: String,
    val to: String,
    val name: String,
    val value: String? = null
) {
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