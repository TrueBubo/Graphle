package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

const val serverURL = "http://localhost:8080/graphql"
const val minUpdateDelay = 1000L

val apolloClient = ApolloClient.Builder()
    .serverUrl(serverURL)
    .build()


@Composable
@Preview
fun App() {
    var location by remember { mutableStateOf("/home") }
    var oldLocation by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableStateOf(0L) }
    var tagName by remember { mutableStateOf("") }
    var tagValue by remember { mutableStateOf("") }
    var file by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column {
            TextField(
                value = location,
                onValueChange = { location = it },
                singleLine = true,
                modifier = Modifier.onPreviewKeyEvent { event ->
                    val canRefresh = !isLoading && ((location != oldLocation) || (currentTimeMillis() - lastUpdated > minUpdateDelay))
                    if (event.key == Key.Enter && canRefresh) {
                        oldLocation = location
                        lastUpdated = currentTimeMillis()
                        coroutineScope.launch {
                            fetchFilesByLocation(
                                location = location,
                                onLoading = { isLoading = it },
                                onResult = { file = it }
                            )
                        }

                        true

                    } else false
                }
            )

            TextField(
                value = tagName,
                onValueChange = { tagName = it },
                singleLine = true,
                modifier = Modifier.onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter ) {
                        coroutineScope.launch {
                            if (tagValue != "") apolloClient.addTagToFile(location, tagName, tagValue)
                            else apolloClient.addTagToFile(location, tagName)
                        }

                        tagName = ""
                        tagValue = ""
                        true
                    } else false
                }
            )

            TextField(
                value = tagValue,
                onValueChange = { tagValue = it },
                singleLine = true,
                modifier = Modifier.onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        coroutineScope.launch {
                            if (tagValue != "") apolloClient.addTagToFile(location, tagName, tagValue)
                            else apolloClient.addTagToFile(location, tagName)
                        }

                        tagName = ""
                        tagValue = ""
                        true
                    } else false
                }
            )

            if (isLoading) {
                Text("Loading...")
            } else {
                Text("File:\n$file")
            }
        }
    }
}

suspend fun fetchFilesByLocation(
    location: String,
    onLoading: (Boolean) -> Unit,
    onResult: (String) -> Unit
) {
    onLoading(true)
    try {
        val response = apolloClient.getFilesByLocation(location)
        onResult(
            if (response.hasErrors()) {
                "Error: ${response.errors?.joinToString()}"
            } else {
                val file = response.data?.fileByLocation
                file?.tags?.joinToString("\n") ?: "No file found"
            }
        )
    } catch (e: Exception) {
        onResult("Network error: ${e.message}")
    }
    onLoading(false)
}

suspend fun ApolloClient.getFilesByLocation(location: String): ApolloResponse<FileByLocationQuery.Data> =
    query(FileByLocationQuery(location)).execute()


suspend fun ApolloClient.addTagToFile(location: String, name: String): ApolloResponse<AddTagToFileWithNameMutation.Data>  =
    mutation(AddTagToFileWithNameMutation(location, name)).execute()

suspend fun ApolloClient.addTagToFile(location: String, name: String, value: String): ApolloResponse<AddTagToFileWithNameAndValueMutation.Data> =
    mutation(AddTagToFileWithNameAndValueMutation(location, name, value)).execute()


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
