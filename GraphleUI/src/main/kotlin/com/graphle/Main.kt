package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import kotlin.time.Duration.Companion.milliseconds

const val serverURL = "http://localhost:8080/graphql"
val minUpdateDelay = 1000.milliseconds

val apolloClient = ApolloClient.Builder()
    .serverUrl(serverURL)
    .build()

enum class PropertyType {
    FILE, TAG, CONNECTION
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var location by remember { mutableStateOf("/home") }
    var oldLocation by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableStateOf(0L) }
    var tagName by remember { mutableStateOf("Name") }
    var tagValue by remember { mutableStateOf("Value") }
    var displayedInfo by remember { mutableStateOf<Map<PropertyType, List<String>>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme(colors = if (isDarkTheme) DarkColorPalette else LightColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column {
                CommandLine()

                TextField(
                    value = location,
                    onValueChange = { location = it },
                    singleLine = true,
                    modifier = Modifier.onPreviewKeyEvent { event ->
                        val canRefresh = !isLoading && ((location != oldLocation)
                                || (currentTimeMillis() - lastUpdated > minUpdateDelay.inWholeMilliseconds))
                        if (event.key == Key.Enter && canRefresh) {
                            oldLocation = location
                            lastUpdated = currentTimeMillis()
                            coroutineScope.launch {
                                fetchFilesByLocation(
                                    location = location,
                                    onLoading = { isLoading = it },
                                    onResult = { displayedInfo = it }
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
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            val tagNameSnapshot = tagName
                            val tagValueSnapshot = tagValue
                            coroutineScope.launch {
                                if (tagNameSnapshot == "") return@launch
                                if (tagValue != "") apolloClient.addTagToFile(
                                    location,
                                    tagNameSnapshot,
                                    tagValueSnapshot
                                )
                                else apolloClient.addTagToFile(location, tagNameSnapshot)
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
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            val tagNameSnapshot = tagName
                            val tagValueSnapshot = tagValue
                            coroutineScope.launch {
                                if (tagNameSnapshot == "") return@launch
                                if (tagValueSnapshot != "") apolloClient.addTagToFile(
                                    location,
                                    tagNameSnapshot,
                                    tagValueSnapshot
                                )
                                else apolloClient.addTagToFile(location, tagNameSnapshot)
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
                    Text("File:")
                    if (displayedInfo == null) Text("Could not find the file")
                    else {
                        displayedInfo?.get(PropertyType.FILE)
                            ?.apply { Text("Files") }
                            ?.forEach { filename ->
                                Text(
                                    text = filename,
                                    modifier = Modifier.onClick(
                                        onClick = {
                                            println("Clicked $filename")
                                            coroutineScope.launch {
                                                fetchFilesByLocation(
                                                    location = filename,
                                                    onLoading = { isLoading = it },
                                                    onResult = {
                                                        location = filename
                                                        displayedInfo = it
                                                    }
                                                )
                                            }
                                        }
                                    )
                                )
                            }
                        displayedInfo?.get(PropertyType.TAG)
                            ?.apply { Text("Tags") }
                            ?.forEach {
                                Text(
                                    text = it,
                                    modifier = Modifier.onClick(onClick = { println("Clicked $it") })
                                )
                            }

                        displayedInfo?.get(PropertyType.CONNECTION)
                            ?.apply { Text("Connections") }
                            ?.forEach { relationshipName ->
                                Text(
                                    text = relationshipName,
                                    modifier = Modifier.onClick(onClick = {
                                        println("Clicked $relationshipName")
                                        coroutineScope.launch {
                                            fetchFilesFromFileByRelationship(
                                                fromLocation = location,
                                                relationshipName = relationshipName,
                                                onLoading = { isLoading = it },
                                                onResult = {
                                                    displayedInfo = mapOf(PropertyType.FILE to (it ?: emptyList()))
                                                }
                                            )
                                        }
                                    })
                                )
                            }
                    }
                }

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )

            }
        }
    }
}

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

suspend fun ApolloClient.getFilesFromFileByRelationship(
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

suspend fun ApolloClient.getFilesByLocation(location: String): ApolloResponse<FileByLocationQuery.Data> =
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


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
