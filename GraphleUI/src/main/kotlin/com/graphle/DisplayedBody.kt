package com.graphle

import androidx.compose.runtime.Composable
import com.graphle.fileWithTag.components.FilesWithTagBody
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import com.graphle.file.components.FileBody

object DisplayedBody {
    @Composable
    operator fun invoke(
        location: String,
        mode: DisplayMode,
        setMode: (DisplayMode) -> Unit,
        displayedData: DisplayedData?,
        setLocation: (String) -> Unit,
        setDisplayedData: (DisplayedData?) -> Unit,
    ) = when (mode) {
        DisplayMode.MainBody -> FileBody(
            location = location,
            displayedData = displayedData,
            setMode = setMode,
            setLocation = setLocation,
            setDisplayedData = setDisplayedData,
        )
        DisplayMode.FilesWithTag -> FilesWithTagBody(
            displayedData = displayedData,
            setMode = setMode,
            setLocation = setLocation,
            setDisplayedData = setDisplayedData,
        )
    }
}