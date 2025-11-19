package com.graphle.header.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import com.graphle.common.model.DisplayedSettings
import com.graphle.dialogs.InvalidFileMessage

/**
 * Application header component with menu and command line.
 *
 * @param setDisplayedSettings Callback to update displayed settings
 * @param getDisplayedSettings Function providing current displayed settings
 * @param setDarkMode Callback to set dark mode
 * @param getDarkMode Function providing current dark mode state
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Header(
    setDisplayedSettings: (DisplayedSettings) -> Unit,
    getDisplayedSettings: () -> DisplayedSettings,
    setDarkMode: (Boolean) -> Unit,
    getDarkMode: () -> Boolean
) {
    TopBar(
        onResult = {
            InvalidFileMessage.showInvalidFileMessage = true
            setDisplayedSettings(it)
        },
        getDisplayedSettings = getDisplayedSettings,
        setDarkMode = setDarkMode,
        getDarkMode = getDarkMode
    )
}
