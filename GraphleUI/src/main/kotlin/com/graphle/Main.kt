package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Desktop
import java.io.File
import java.lang.System.currentTimeMillis
import kotlin.time.Duration.Companion.milliseconds

const val serverURL = "http://localhost:8080/graphql"
val minUpdateDelay = 1000.milliseconds

val apolloClient = ApolloClient.Builder()
    .serverUrl(serverURL)
    .build()

data class DisplayedData(
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList()
)

private fun theme(isDarkTheme: Boolean): Colors =
    if (isDarkTheme) DarkColorPalette else LightColorPalette

fun openFile(file: File) {
    if (!file.exists()) {
        return
    }

    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file)
        } else {
            println("OPEN action is not supported on this platform")
        }
    } else {
        println("Desktop API is not supported")
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(setTitle: (String) -> Unit = {}) {
    var location by remember { mutableStateOf("/home") }
    var oldLocation by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableStateOf(0L) }
    var tagName by remember { mutableStateOf("Name") }
    var tagValue by remember { mutableStateOf("Value") }
    var isLoading by remember { mutableStateOf(false) }
    var showHiddenFiles by remember { mutableStateOf(false) }
    var displayedData by remember {
        mutableStateOf(
            runBlocking {
                fetchFilesByLocation(
                    location = location,
                    showHiddenFiles = showHiddenFiles,
                    onLoading = { isLoading = it },
                    onResult = { }
                )
            }
        )
    }

    var showInvalidFileDialog by remember { mutableStateOf(true) }
    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }
    val coroutineScope = rememberCoroutineScope()

    setTitle("Graphle - $location")

    println("Show hidden files: $showHiddenFiles")

    MaterialTheme(colors = theme(isDarkTheme = isDarkTheme)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                item {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                    Row {
                        TopBar(
                            location = location,
                            showHiddenFiles = showHiddenFiles,
                            setShowHiddenFiles = { showHiddenFiles = it },
                            onLoading = { isLoading = it },
                            onResult = {
                                showInvalidFileDialog = true
                                displayedData = it
                            }
                        )
                        Text("Text")
                    }
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
                                        showHiddenFiles = showHiddenFiles,
                                        onResult = { info ->
                                            showInvalidFileDialog = true
                                            displayedData = info
                                        }
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
                }

                if (isLoading) {
                    item {
                        Text("Loading...")
                    }
                } else {
                    if (displayedData == null) {
                        item {
                            Text("Could not find the file")
                            if (showInvalidFileDialog) AlertDialog(
                                onDismissRequest = { showInvalidFileDialog = false },
                                title = { Text("Error") },
                                text = { Text("Could not find the file at $location") },
                                confirmButton = {
                                    TextButton(onClick = { showInvalidFileDialog = false }) {
                                        Text("OK")
                                    }
                                }
                            )
                        }
                    } else {
                        item {
                            TagsView(
                                displayedData = displayedData,
                                colors = theme(isDarkTheme)
                            )
                        }

                        item {
                            FilesView(
                                displayedData = displayedData,
                                showHiddenFiles = showHiddenFiles,
                                onLoading = { isLoading = it },
                                setLocation = {
                                    location = it
                                    setTitle("Graphle - $location")
                                },
                                setDisplayedInfo = {
                                    displayedData = it
                                    showInvalidFileDialog = true
                                },
                                coroutineScope = coroutineScope,
                            )
                        }
                    }
                }
            }
        }
    }
}

const val minWidthPx = 600
const val minHeightPx = 400
fun main() = application {
    var title by remember { mutableStateOf("Graphle") }
    Window(onCloseRequest = ::exitApplication, title = title) {
        window.minimumSize = java.awt.Dimension(minWidthPx, minHeightPx)
        App(setTitle = { title = it })
    }
}
