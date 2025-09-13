package com.graphle

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse

suspend fun fetchFilesFromFileByRelationship(
    fromLocation: String,
    relationshipName: String,
    onLoading: (Boolean) -> Unit,
    onResult: (List<Connection>?) -> Unit
) {
    onLoading(true)
    println("Ran relation")
    val response = apolloClient.getFilesFromFileByRelationship(
        fromLocation = fromLocation,
        relationshipName = relationshipName
    )
    onResult(if (response.hasErrors()) null else response.data?.filesFromFileByRelationship?.map {
        Connection(
            name = relationshipName,
            value = it.value,
            to = it.to
        )
    })
    onLoading(false)
}

private suspend fun ApolloClient.getFilesFromFileByRelationship(
    fromLocation: String,
    relationshipName: String
): ApolloResponse<FilesFromFileByRelationshipQuery.Data> =
    query(FilesFromFileByRelationshipQuery(fromLocation, relationshipName)).execute()

suspend fun fetchFilesByLocation(
    location: String,
    onLoading: (Boolean) -> Unit,
    onResult: (DisplayedInfo?) -> Unit
) {
    onLoading(true)
    val response = apolloClient.getFilesByLocation(location)
    println(response.data?.fileByLocation)
    onResult(
        if (response.hasErrors()) {
            null
        } else {
            val file = response.data?.fileByLocation
            if (file != null)
                DisplayedInfo(
                    tags = file.tags.map { Tag(it.name, it.value) },
                    connections = file.connections.map {
                        Connection(
                            name = it.name,
                            value = it.value,
                            to = it.to
                        )
                    }
                )
            else null
        }
    )
    onLoading(false)
}

private suspend fun ApolloClient.getFilesByLocation(location: String): ApolloResponse<FileByLocationQuery.Data> =
    query(FileByLocationQuery(location)).execute()


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