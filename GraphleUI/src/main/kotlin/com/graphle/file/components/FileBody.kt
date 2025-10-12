package com.graphle.file.components

import androidx.compose.runtime.Composable
import com.graphle.tag.components.TagsView
import com.graphle.file.util.FileFetcher
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData

@Composable
fun FileBody(
    location: String,
    displayedData: DisplayedData?,
    setMode: (DisplayMode) -> Unit,
    setLocation: (String) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
) {
    TagsView(
        location = location,
        displayedData = displayedData,
        setDisplayedData = setDisplayedData,
        setMode = setMode,
        onRefresh = {
            FileFetcher.fetch(
                location = location,
                onResult = { displayedInfo ->
                    setDisplayedData(
                        DisplayedData(
                            tags = displayedInfo?.tags ?: emptyList(),
                            connections = displayedInfo?.connections ?: emptyList(
                            )
                        )
                    )
                }
            )
        }
    )

    FilesView(
        displayedData = displayedData,
        setLocation = setLocation,
        setDisplayedData = setDisplayedData,
    )
}