package com.graphle.fileWithTag.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graphle.tag.model.Tag
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import com.graphle.common.supervisorIoScope
import com.graphle.common.ui.Pill
import com.graphle.file.util.FileFetcher
import kotlinx.coroutines.launch
import kotlin.collections.forEach

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
                            setMode(DisplayMode.File)
                        }
                    }
                )
            }
        }
    }
}