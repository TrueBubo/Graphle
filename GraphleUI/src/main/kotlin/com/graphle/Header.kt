package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Header(
    location: String,
    setLocation: (String) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
    setDarkMode: (Boolean) -> Unit,
    getDarkMode: () -> Boolean
) {
    TopBar(
        location = location,
        setLocation = setLocation,
        onResult = {
            InvalidFileMessage.showInvalidFileMessage = true
            setDisplayedData(it)
        },
        setDarkMode = setDarkMode,
        getDarkMode = getDarkMode
    )
}
