package com.graphle

import androidx.compose.runtime.Composable

@Composable
fun MainBody(
    displayedData: DisplayedData?,
    setMode: (DisplayMode) -> Unit,
    setLocation: (String) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
) {
    TagsView(
        displayedData = displayedData,
        setDisplayedData = setDisplayedData,
        setMode = setMode,
    )

    FilesView(
        displayedData = displayedData,
        setLocation = setLocation,
        setDisplayedData = setDisplayedData,
    )
}