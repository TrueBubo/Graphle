package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Header(
    location: String,
    setDisplayedData: (DisplayedData?) -> Unit,
) {
    TopBar(
        location = location,
        onResult = {
            InvalidFileDialog.showInvalidFileDialog = true
            setDisplayedData(it)
        }
    )
}
