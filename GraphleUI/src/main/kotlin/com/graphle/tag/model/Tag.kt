package com.graphle.tag.model

import com.graphle.common.addTagToFile
import com.graphle.common.apolloClient
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val name: String,
    val value: String?
) {
    suspend fun save(location: String) {
        if (value != null) apolloClient.addTagToFile(
            location,
            name,
            value
        )
        else apolloClient.addTagToFile(location, name)
    }
}