package com.graphle.dialogs

import androidx.compose.runtime.Composable
import com.graphle.common.model.DisplayMode
import com.graphle.file.util.FileFetcher
import com.graphle.common.model.DisplayedSettings
import com.graphle.dsl.DSLHistory

/**
 * Composite component that renders all application dialogs.
 *
 * @param setDisplayedSettings Callback to update displayed settings
 * @param getDisplayedSettings Function providing current displayed settings
 * @param isInvalidFile Whether the current file is invalid
 */
@Composable
fun Dialogs(
    setDisplayedSettings: (DisplayedSettings) -> Unit,
    getDisplayedSettings: () -> DisplayedSettings,
    isInvalidFile: Boolean,
) {
    val location = getDisplayedSettings().data?.location ?: return
    AddTagDialog(
        onSubmitted = {
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedSettings
            )
        }
    )

    AddRelationshipDialog(
        onSubmitted = {
            if (getDisplayedSettings().mode != DisplayMode.File) return@AddRelationshipDialog
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedSettings
            )
        },
        onUpdatedData = { getDisplayedSettings().data }
    )

    AddFileDialog(
        onConfirmed = {
            if (getDisplayedSettings().mode != DisplayMode.File) return@AddFileDialog
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedSettings
            )
        }
    )

    MoveFileDialog(
        onMoved = {
            DSLHistory.repeatLastDisplayedCommand(setDisplayedSettings)
        },
    )

    DeleteFileDialog(
        onConfirmed = {
            FileFetcher.fetch(
                location = location,
                onResult = {
                    setDisplayedSettings(it)
                }
            )
        }
    )

    ErrorMessage()

    InvalidFileMessage(
        isInvalidFile = isInvalidFile,
    )
}