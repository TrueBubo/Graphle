package com.graphle.dialogs

import androidx.compose.runtime.Composable
import com.graphle.file.util.FileFetcher
import com.graphle.common.model.DisplayedData

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

    DeleteFileDialog(
        onConfirmed = {
            FileFetcher.fetch(
                location = location,
                onResult = {
                    setDisplayedData(it)
                }
            )
        }
    )

    ErrorMessage()

    InvalidFileMessage(
        location = location,
        isInvalidFile = isInvalidFile,
    )
}