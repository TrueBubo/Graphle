package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Header(
    location: String,
    setDisplayedData: (DisplayedData?) -> Unit,
    setDarkMode: (Boolean) -> Unit,
    getDarkMode: () -> Boolean
) {
    TopBar(
        location = location,
        onResult = {
            InvalidFileMessage.showInvalidFileMessage = true
            setDisplayedData(it)
        },
        setDarkMode = setDarkMode,
        getDarkMode = getDarkMode
    )
}
