package com.graphle

import androidx.compose.runtime.Composable
import com.graphle.fileWithTag.components.FilesWithTagBody
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedSettings
import com.graphle.file.components.FileBody
import com.graphle.file.components.FilenameBody

object DisplayedBody {
    @Composable
    operator fun invoke(
        displayedSettings: DisplayedSettings,
        setDisplayedSettings: (DisplayedSettings) -> Unit,
    ) {
        val location = displayedSettings.data?.location ?: ""
        return when (displayedSettings.mode) {
            DisplayMode.File -> FileBody(
                displayedSettings = displayedSettings,
                setDisplayedSettings = setDisplayedSettings,
            )

            DisplayMode.FilesWithTag -> FilesWithTagBody(
                displayedSettings = displayedSettings,
                setDisplayedSettings = setDisplayedSettings,
            )

            DisplayMode.Filenames -> FilenameBody(
                displayedSettings = displayedSettings,
                setDisplayedSettings = setDisplayedSettings,
            )

            DisplayMode.Connections -> FileBody(
                displayedSettings = displayedSettings,
                setDisplayedSettings = setDisplayedSettings,
            )
        }
    }
}