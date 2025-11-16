package com.graphle.file.components

import FilenamesView
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.graphle.common.model.DisplayedSettings

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilenameBody(
    displayedSettings: DisplayedSettings,
    setDisplayedSettings: (DisplayedSettings) -> Unit,
) {
    FilenamesView(
        displayedSettings = displayedSettings,
        setDisplayedSettings = setDisplayedSettings,
    )
}