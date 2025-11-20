package com.graphle.file.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.graphle.file.model.Connection
import com.graphle.FileByLocationQuery
import com.graphle.tag.model.Tag
import com.graphle.common.apolloClient
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings
import com.graphle.dsl.DSLHistory

private fun fileFetchDSLCommand(location: String) = "detail $location"

/**
 * Fetches file information from the server and manages loading state.
 */
object FileFetcher {
    /**
     * Whether to show hidden files in directory listings.
     */
    var showHiddenFiles = false

    private var _isLoading = false

    /**
     * Indicates whether a fetch operation is currently in progress.
     */
    val isLoading: Boolean
        get() = _isLoading

    /**
     * Fetches file data by location from the server.
     *
     * @param location File path to fetch
     * @param onResult Callback invoked with the fetched settings
     * @return DisplayedSettings containing the fetched data
     */
    suspend fun fetch(
        location: String,
        onResult: (DisplayedSettings) -> Unit
    ): DisplayedSettings {
        DSLHistory.lastDisplayedCommand.value = fileFetchDSLCommand(location)
        return fetchFilesByLocation(
            location = location,
            showHiddenFiles = showHiddenFiles,
            onResult = onResult
        )
    }

    private suspend fun fetchFilesByLocation(
        location: String,
        showHiddenFiles: Boolean,
        onResult: (DisplayedSettings) -> Unit
    ): DisplayedSettings {
        _isLoading = true
        val response = apolloClient.getFilesByLocation(location, showHiddenFiles)
        val result = if (response.hasErrors()) {
            null
        } else {
            val file = response.data?.fileByLocation
            if (file != null)
                DisplayedData(
                    location = location,
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
        val settings = DisplayedSettings(
            data = result,
            mode = DisplayMode.File
        )
        onResult(settings)
        _isLoading = false
        return settings
    }

    private suspend fun ApolloClient.getFilesByLocation(
        location: String,
        showHiddenFiles: Boolean
    ): ApolloResponse<FileByLocationQuery.Data> =
        query(FileByLocationQuery(location, showHiddenFiles)).execute()

}