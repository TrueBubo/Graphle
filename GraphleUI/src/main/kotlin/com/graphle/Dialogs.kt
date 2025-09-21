package com.graphle

import androidx.compose.runtime.Composable

@Composable
fun Dialogs(
    location: String,
    setDisplayedData: (DisplayedData?) -> Unit,
    isInvalidFile: Boolean,
) {
    AddTagDialog(
        onSubmitted = {
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedData
            )
        }
    )

    InvalidFileDialog(
        location = location,
        isInvalidFile = isInvalidFile,
    )
}