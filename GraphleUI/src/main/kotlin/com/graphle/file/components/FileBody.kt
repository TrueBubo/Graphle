package com.graphle.file.components

import androidx.compose.runtime.Composable
import com.graphle.tag.components.TagsView
import com.graphle.file.util.FileFetcher
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings

@Composable
fun FileBody(
    displayedSettings: DisplayedSettings,
    setDisplayedSettings: (DisplayedSettings) -> Unit,
) {
    val location = displayedSettings.data?.location ?: ""
    TagsView(
        location = location,
        displayedSettings = displayedSettings,
        setDisplayedSettings = setDisplayedSettings,
        onRefresh = {
            FileFetcher.fetch(
                location = location,
                onResult = setDisplayedSettings
            )
        }
    )

    FilesView(
        displayedSettings = displayedSettings,
        setDisplayedSettings = setDisplayedSettings,
    )
}