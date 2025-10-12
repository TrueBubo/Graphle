package com.graphle.file.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.graphle.file.model.Connection
import com.graphle.FileByLocationQuery
import com.graphle.tag.model.Tag
import com.graphle.common.apolloClient
import com.graphle.common.model.DisplayedData

object FileFetcher {
    var showHiddenFiles = false
    private var _isLoading = false
    val isLoading: Boolean
        get() = _isLoading

    suspend fun fetch(
        location: String,
        onResult: (DisplayedData?) -> Unit
    ) = fetchFilesByLocation(
        location = location,
        showHiddenFiles = showHiddenFiles,
        onResult = onResult
    )

    private suspend fun fetchFilesByLocation(
        location: String,
        showHiddenFiles: Boolean,
        onResult: (DisplayedData?) -> Unit
    ): DisplayedData? {
        _isLoading = true
        val response = apolloClient.getFilesByLocation(location, showHiddenFiles)
        val result = if (response.hasErrors()) {
            null
        } else {
            val file = response.data?.fileByLocation
            if (file != null)
                DisplayedData(
                    tags = file.tags.map { Tag(it.name, it.value) },
                    connections = file.connections.map {
                        Connection(
                            name = it.name,
                            value = it.value,
                            from = it.from,
                            to = it.to
                        )
                    }
                )
            else null
        }
        onResult(result)
        _isLoading = false
        return result
    }

    private suspend fun ApolloClient.getFilesByLocation(
        location: String,
        showHiddenFiles: Boolean
    ): ApolloResponse<FileByLocationQuery.Data> =
        query(FileByLocationQuery(location, showHiddenFiles)).execute()

}