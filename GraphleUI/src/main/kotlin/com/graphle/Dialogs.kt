package com.graphle

import androidx.compose.runtime.Composable

@Composable
fun Dialogs(
    location: String,
    setDisplayedData: (DisplayedData?) -> Unit,
    getDisplayedData: () -> DisplayedData?,
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

    AddRelationshipDialog(
        onSubmitted = {
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedData
            )
        },
        onUpdatedData = getDisplayedData
    )

    AddFileDialog(
        onConfirmed = {
            FileFetcher.fetch(
                location = location,
                onResult = {
                    setDisplayedData(it)
                }
            )
        }
    )

    MoveFileDialog(
        onMoved = {
            FileFetcher.fetch(
                location = location,
                onResult = {
                    setDisplayedData(it)
                }
            )
        },

    )

    ErrorMessage()

    InvalidFileMessage(
        location = location,
        isInvalidFile = isInvalidFile,
    )
}