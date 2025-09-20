package com.graphle

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