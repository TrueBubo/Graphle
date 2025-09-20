package com.graphle

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse

suspend fun fetchFilesFromFileByRelationship(
    fromLocation: String,
    relationshipName: String,
    onLoading: (Boolean) -> Unit,
    onResult: (List<Connection>?) -> Unit
): List<Connection>? {
    onLoading(true)
    val response = apolloClient.getFilesFromFileByRelationship(
        fromLocation = fromLocation,
        relationshipName = relationshipName
    )
    val result = if (response.hasErrors()) null else response.data?.filesFromFileByRelationship?.map {
        Connection(
            from = it.from,
            to = it.to,
            name = relationshipName,
            value = it.value
        )
    }
    onResult(result)
    onLoading(false)
    return result
}

private suspend fun ApolloClient.getFilesFromFileByRelationship(
    fromLocation: String,
    relationshipName: String
): ApolloResponse<FilesFromFileByRelationshipQuery.Data> =
    query(FilesFromFileByRelationshipQuery(fromLocation, relationshipName)).execute()

suspend fun fetchFilesByLocation(
    location: String,
    showHiddenFiles: Boolean,
    onLoading: (Boolean) -> Unit,
    onResult: (DisplayedData?) -> Unit
): DisplayedData? {
    onLoading(true)
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
    onLoading(false)
    return result
}

private suspend fun ApolloClient.getFilesByLocation(location: String, showHiddenFiles: Boolean): ApolloResponse<FileByLocationQuery.Data> =
    query(FileByLocationQuery(location, showHiddenFiles)).execute()


suspend fun ApolloClient.addTagToFile(
    location: String,
    name: String
): ApolloResponse<AddTagToFileWithNameMutation.Data> =
    mutation(AddTagToFileWithNameMutation(location, name)).execute()

suspend fun ApolloClient.addTagToFile(
    location: String,
    name: String,
    value: String
): ApolloResponse<AddTagToFileWithNameAndValueMutation.Data> =
    mutation(AddTagToFileWithNameAndValueMutation(location, name, value)).execute()

suspend fun ApolloClient.removeFileByLocation(
    location: String
): ApolloResponse<RemoveFileMutation.Data> =
    mutation(RemoveFileMutation(location)).execute()

suspend fun ApolloClient.moveFile(
    locationFrom: String,
    locationTo: String
): ApolloResponse<MoveFileMutation.Data> =
    mutation(MoveFileMutation(locationFrom, locationTo)).execute()