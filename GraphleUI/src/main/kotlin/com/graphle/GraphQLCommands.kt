package com.graphle

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.graphle.type.FileType

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

suspend fun ApolloClient.addRelationshipToFile(
    from: String,
    to: String,
    name: String,
    bidirectional: Boolean
): ApolloResponse<AddRelationshipToFileWithNameMutation.Data> =
    mutation(AddRelationshipToFileWithNameMutation(from, to, name, bidirectional)).execute()

suspend fun ApolloClient.addRelationshipToFile(
    from: String,
    to: String,
    name: String,
    value: String,
    bidirectional: Boolean
): ApolloResponse<AddRelationshipToFileWithNameAndValueMutation.Data> =
    mutation(AddRelationshipToFileWithNameAndValueMutation(from, to, name, value, bidirectional)).execute()


suspend fun ApolloClient.removeFileByLocation(
    location: String
): ApolloResponse<RemoveFileMutation.Data> =
    mutation(RemoveFileMutation(location)).execute()

suspend fun ApolloClient.moveFile(
    locationFrom: String,
    locationTo: String
): ApolloResponse<MoveFileMutation.Data> =
    mutation(MoveFileMutation(locationFrom, locationTo)).execute()

private suspend fun ApolloClient.fileType(
    location: String
): ApolloResponse<FileTypeQuery.Data> = query(FileTypeQuery(location)).execute()

suspend fun fileType(location: String): FileType? = apolloClient.fileType(location).data?.fileType

private suspend fun ApolloClient.addFile(
    location: String
): ApolloResponse<AddFileMutation.Data> = mutation(AddFileMutation(location)).execute()

suspend fun addFile(location: String) = apolloClient.addFile(location)

private suspend fun ApolloClient.filesByTag(
    tagName: String
): ApolloResponse<FilesByTagQuery.Data> = query(FilesByTagQuery(tagName)).execute()

suspend fun filesByTag(
    tagName: String
): List<FileWithTag>? = apolloClient.filesByTag(tagName).data?.filesByTag?.map {
    FileWithTag(
        location = it.location,
        tag = Tag(
            name = it.tag.name,
            value = it.tag.value
        )
    )
}