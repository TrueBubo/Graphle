package com.graphle

import androidx.compose.runtime.Composable

object DisplayedBody {
    @Composable
    operator fun invoke(
        mode: DisplayMode,
        setMode: (DisplayMode) -> Unit,
        displayedData: DisplayedData?,
        setLocation: (String) -> Unit,
        setDisplayedData: (DisplayedData?) -> Unit,
    ) = when (mode) {
        DisplayMode.MainBody -> MainBody(
            displayedData = displayedData,
            setMode = setMode,
            setLocation = setLocation,
            setDisplayedData = setDisplayedData,
        )
        DisplayMode.FilesWithTag -> FilesWithTagBody(
            displayedData = displayedData,
            setMode = setMode,
            setLocation = setLocation,
            setDisplayedData = setDisplayedData,
        )
    }
}