package com.graphle.header.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import com.graphle.common.model.DisplayedData
import com.graphle.dialogs.InvalidFileMessage

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
