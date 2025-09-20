package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis

data class DisplayedData(
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(setTitle: (String) -> Unit = {}) {
    var location by remember { mutableStateOf(userHome) }
    var oldLocation by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableStateOf(0L) }
    var displayedData by remember {
        mutableStateOf(runBlocking {
            FileFetcher.fetch(
                location = location,
                onResult = { }
            )
        }
        )
    }

    var showInvalidFileDialog by remember { mutableStateOf(true) }
    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }

    setTitle("Graphle - $location")

    MaterialTheme(colors = if (isDarkTheme) DarkColorPalette else LightColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            AddTagDialog(
                onSubmitted = {
                    FileFetcher.fetch(
                        location = location,
                        onResult = { info ->
                            showInvalidFileDialog = true
                            displayedData = info
                        }
                    )
                }
            )
            LazyColumn {
                item {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                    TopBar(
                        location = location,
                        onResult = {
                            showInvalidFileDialog = true
                            displayedData = it
                        }
                    )
                    TextField(
                        value = location,
                        onValueChange = { location = it },
                        singleLine = true,
                        modifier = Modifier.onPreviewKeyEvent { event ->
                            val canRefresh = (location != oldLocation)
                                    || (currentTimeMillis() - lastUpdated > minUpdateDelay.inWholeMilliseconds)
                            if (event.key == Key.Enter && canRefresh) {
                                oldLocation = location
                                lastUpdated = currentTimeMillis()

                                supervisorIoScope.launch {
                                    FileFetcher.fetch(
                                        location = location,
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
                }

                if (FileFetcher.isLoading) {
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
                            )
                        }

                        item {
                            FilesView(
                                displayedData = displayedData,
                                setLocation = {
                                    location = it
                                    setTitle("Graphle - $location")
                                },
                                setDisplayedInfo = {
                                    displayedData = it
                                    showInvalidFileDialog = true
                                },
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
