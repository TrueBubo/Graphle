package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.apollographql.apollo.ApolloClient
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

                TagTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    location = location,
                    tagName = tagName,
                    tagValue = tagValue,
                    tagNameSetter = { tagName = it },
                    tagValueSetter = { tagValue = it },
                    coroutineScope = coroutineScope,
                )

                TagTextField(
                    value = tagValue,
                    onValueChange = { tagValue = it },
                    location = location,
                    tagName = tagName,
                    tagValue = tagValue,
                    tagNameSetter = { tagName = it },
                    tagValueSetter = { tagValue = it },
                    coroutineScope = coroutineScope,
                )

                if (isLoading) {
                    Text("Loading...")
                } else {
                    Text("File:")
                    if (displayedInfo == null) Text("Could not find the file")
                    else {
                        displayedInfo?.get(PropertyType.FILE)
                            ?.apply { Text(text = "Files", fontWeight = FontWeight.Bold) }
                            ?.let { filenames ->
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(items = filenames, key = { it }) { filename ->
                                        FileBox(
                                            filename = filename,
                                            onLoading = { isLoading = it },
                                            onResult = {
                                                location = filename
                                                displayedInfo = it
                                            },
                                            onRefresh = {
                                                coroutineScope.launch {
                                                    fetchFilesFromFileByRelationship(
                                                        fromLocation = location,
                                                        relationshipName = "descendant",
                                                        onLoading = { isLoading = it },
                                                        onResult = {
                                                            displayedInfo =
                                                                mapOf(PropertyType.FILE to (it ?: emptyList()))
                                                        }
                                                    )
                                                }
                                            },
                                            coroutineScope = coroutineScope
                                        )
                                    }
                                }
                            }
                        displayedInfo?.get(PropertyType.TAG)
                            ?.apply { Text(text = "Tags", fontWeight = FontWeight.Bold) }
                            ?.forEach {
                                Text(
                                    text = it,
                                    modifier = Modifier.onClick(onClick = { println("Clicked $it") })
                                )
                            }

                        displayedInfo?.get(PropertyType.CONNECTION)
                            ?.apply { Text(text = "Connections", fontWeight = FontWeight.Bold) }
                            ?.forEach { relationshipName ->
                                RelationshipBox(
                                    relationshipName = relationshipName,
                                    location = location,
                                    onLoading = { isLoading = it },
                                    onResult = {
                                        displayedInfo = mapOf(PropertyType.FILE to (it ?: emptyList()))
                                    },
                                    onRefresh = {
                                        coroutineScope.launch {
                                            fetchFilesByLocation(
                                                location = location,
                                                onLoading = { isLoading = it },
                                                onResult = { displayedInfo = it }
                                            )
                                        }
                                    },
                                    coroutineScope = coroutineScope
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

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
