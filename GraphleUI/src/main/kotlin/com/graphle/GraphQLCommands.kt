package com.graphle

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse

suspend fun fetchFilesFromFileByRelationship(
    fromLocation: String,
    relationshipName: String,
    onLoading: (Boolean) -> Unit,
    onResult: (List<String>?) -> Unit
) {
    onLoading(true)
    println("Ran relation")
    val response = apolloClient.getFilesFromFileByRelationship(
        fromLocation = fromLocation,
        relationshipName = relationshipName
    )
    onResult(if (response.hasErrors()) null else response.data?.filesFromFileByRelationship)
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
    onResult: (Map<PropertyType, List<String>>?) -> Unit
) {
    onLoading(true)
    val response = apolloClient.getFilesByLocation(location)
    onResult(
        if (response.hasErrors()) {
            null
        } else {
            val file = response.data?.fileByLocation
            if (file != null) mapOf(
                PropertyType.TAG to file.tags.map { "${it.name}: ${it.value ?: ""}" },
                PropertyType.CONNECTION to file.connections.map { it.relationship }
            ) else null
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