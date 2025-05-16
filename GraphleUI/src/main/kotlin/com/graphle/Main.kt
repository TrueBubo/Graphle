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

const val serverURL = "http://localhost:8080/graphql"

@Composable
@Preview
fun App() {
    var location by remember { mutableStateOf("/home") }
    var oldLocation by remember { mutableStateOf("") }
    var file by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column {
            TextField(
                value = location,
                onValueChange = { location = it },
                singleLine = true,
                modifier = Modifier.onPreviewKeyEvent {
                    if (it.key == Key.Enter && !isLoading && oldLocation != location) {
                        oldLocation = location
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
        val response = getFilesByLocation(location)
        onResult(
            if (response.hasErrors()) {
                "Error: ${response.errors?.joinToString()}"
            } else {
                response.data?.fileByLocation?.tags?.joinToString("\n") ?: "No file found"
            }
        )
    } catch (e: Exception) {
        onResult("Network error: ${e.message}")
    }
    onLoading(false)
}

suspend fun getFilesByLocation(location: String): ApolloResponse<FileByLocationQuery.Data> {
    val apolloClient = ApolloClient.Builder()
        .serverUrl(serverURL)
        .build()

    return apolloClient.query(FileByLocationQuery(location)).execute()
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
