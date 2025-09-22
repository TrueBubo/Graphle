package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
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
import com.graphle.InvalidFileDialog.showInvalidFileDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis

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

    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }

    setTitle("Graphle - $location")

    MaterialTheme(colors = if (isDarkTheme) DarkColorPalette else LightColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Dialogs(
                location = location,
                setDisplayedData = { displayedData = it },
                getDisplayedData = { displayedData },
                isInvalidFile = displayedData == null,
            )

            LazyColumn {
                stickyHeader {
                    Header(
                        location = location,
                        setDisplayedData = { displayedData = it },
                    )
                }
                item {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
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

                        }
                    } else {
                        item {
                            Body(
                                displayedData = displayedData,
                                setLocation = { location = it },
                                setDisplayedData = {
                                    displayedData = it
                                    showInvalidFileDialog = true
                                },
                            )
                        }
//                        item {
//                            TagsView(
//                                displayedData = displayedData,
//                            )
//                        }
//
//                        item {
//                            FilesView(
//                                displayedData = displayedData,
//                                setLocation = {
//                                    location = it
//                                    setTitle("Graphle - $location")
//                                },
//                                setDisplayedData = {
//                                    displayedData = it
//                                    showInvalidFileDialog = true
//                                },
//                            )
//                        }
                    }
                }
            }
        }
    }
}