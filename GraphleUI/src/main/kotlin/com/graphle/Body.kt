package com.graphle

import androidx.compose.runtime.Composable

@Composable
fun Body(
    displayedData: DisplayedData?,
    setLocation: (String) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
) {
    TagsView(
        displayedData = displayedData,
    )


    FilesView(
        displayedData = displayedData,
        setLocation = setLocation,
        setDisplayedData = setDisplayedData,
    )
}