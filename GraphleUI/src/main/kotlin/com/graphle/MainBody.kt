package com.graphle

import androidx.compose.runtime.Composable

@Composable
fun MainBody(
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