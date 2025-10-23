package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.graphle.file.util.FileFetcher
import com.graphle.common.model.DisplayMode
import com.graphle.dialogs.InvalidFileMessage.showInvalidFileMessage
import com.graphle.common.ui.DarkColorPalette
import com.graphle.common.ui.LightColorPalette
import com.graphle.common.userHome
import com.graphle.dialogs.Dialogs
import com.graphle.dialogs.ErrorMessage
import com.graphle.header.components.Header
import kotlinx.coroutines.runBlocking

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

    val mode = remember { mutableStateOf(DisplayMode.MainBody) }
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
                setDisplayedData = {
                    if (displayedData != null) displayedData = it
                    else ErrorMessage.set(
                        showErrorMessage = true,
                        errorMessage = "Could not get data, check whether it exists and you have necessary permissions.",
                    )
                },
                getDisplayedData = { displayedData },
                isInvalidFile = displayedData == null,
            )

            LazyColumn {
                stickyHeader {
                    Header(
                        location = location,
                        setLocation = { location = it },
                        setDisplayedData = { displayedData = it },
                        setDarkMode = { isDarkTheme = it },
                        getDarkMode = { isDarkTheme }
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
                            DisplayedBody(
                                location = location,
                                mode = mode.value,
                                setMode = { mode.value = it },
                                displayedData = displayedData,
                                setLocation = { location = it },
                                setDisplayedData = {
                                    if (it == null) ErrorMessage.set(
                                        showErrorMessage = true,
                                        errorMessage = "Could not load the file, check whether it exists and " +
                                                "you have necessary permissions."
                                    )
                                    else {
                                        displayedData = it
                                        showInvalidFileMessage = true
                                    }
                                },
                            )

                        }
                    }
                }
            }
        }
    }
}