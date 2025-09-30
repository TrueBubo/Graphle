package com.graphle

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import java.net.URI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsView(
    displayedData: DisplayedData?,
    setMode: (DisplayMode) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit
) {
    val tags = displayedData?.tags ?: return

    val (urls, nonUrls) = tags.partition {
        if (it.name.lowercase() == "url") {
            try {
                URI.create(it.value!!).toURL()
                return@partition true
            } catch (_: Exception) {
                return@partition false
            }
        } else false
    }

    Text(text = "URLs", fontWeight = FontWeight.Bold)
    FlowRow {
        urls.forEach {
            TagBox(tag = it, setMode = setMode, setDisplayedData = setDisplayedData)
        }
    }

    Text(text = "Tags", fontWeight = FontWeight.Bold)
    FlowRow {
        nonUrls.forEach {
            TagBox(tag = it, setMode = setMode, setDisplayedData = setDisplayedData)
        }
    }
}