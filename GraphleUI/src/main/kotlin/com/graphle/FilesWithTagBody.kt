package com.graphle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private fun textsForPill(tag: Tag, location: String) = buildList {
    add(tag.name + (tag.value?.let { "=$it" } ?: ""))
    add(location)
}

@Composable
fun FilesWithTagBody(
    displayedData: DisplayedData?,
    setDisplayedData: (DisplayedData?) -> Unit,
    setMode: (DisplayMode) -> Unit,
    setLocation: (String) -> Unit
) {
    Column {
        displayedData?.filesWithTag?.forEach {
            Box(Modifier.padding(bottom = 10.dp)) {
                Pill(
                    texts = textsForPill(tag = it.tag, location = it.location),
                    onClick = {
                        supervisorIoScope.launch {
                            FileFetcher.fetch(location = it.location, onResult = setDisplayedData)
                            setLocation(it.location)
                            setMode(DisplayMode.MainBody)
                        }
                    }
                )
            }
        }
    }
}