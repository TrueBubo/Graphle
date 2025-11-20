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
import com.graphle.dialogs.InvalidFileMessage.showInvalidFileMessage
import com.graphle.common.ui.DarkColorPalette
import com.graphle.common.ui.LightColorPalette
import com.graphle.common.userHome
import com.graphle.dialogs.Dialogs
import com.graphle.dialogs.ErrorMessage
import com.graphle.header.components.Header
import kotlinx.coroutines.runBlocking

/**
 * Main application composable that sets up the UI structure and manages application state.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var displayedSettings by remember {
        mutableStateOf(
            runBlocking {
                FileFetcher.fetch(
                    location = userHome,
                    onResult = { }
                )
            }
        )
    }
    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }

    MaterialTheme(colors = if (isDarkTheme) DarkColorPalette else LightColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Dialogs(
                setDisplayedSettings = { newSettings ->
                    if (newSettings.data != null) {
                        displayedSettings = newSettings
                    } else {
                        ErrorMessage.set(
                            showErrorMessage = true,
                            errorMessage = "Could not get data, check whether it exists and you have necessary permissions.",
                        )
                    }
                },
                getDisplayedSettings = { displayedSettings },
                isInvalidFile = displayedSettings.data == null,
            )

            LazyColumn {
                stickyHeader {
                    Header(
                        setDisplayedSettings = { displayedSettings = it },
                        getDisplayedSettings = { displayedSettings },
                        setDarkMode = { isDarkTheme = it },
                        getDarkMode = { isDarkTheme },
                    )
                }

                if (FileFetcher.isLoading) {
                    item {
                        Text("Loading...")
                    }
                } else {
                    if (displayedSettings.data == null) {
                        item {
                            Text("Could not find the file")

                        }
                    } else {
                        item {
                            DisplayedBody(
                                displayedSettings = displayedSettings,
                                setDisplayedSettings = {
                                    if (it.data == null) ErrorMessage.set(
                                        showErrorMessage = true,
                                        errorMessage = "Could not load the file, check whether it exists and " +
                                                "you have necessary permissions."
                                    )
                                    else {
                                        displayedSettings = it
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