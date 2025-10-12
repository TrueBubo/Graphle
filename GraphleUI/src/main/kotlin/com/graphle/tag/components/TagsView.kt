package com.graphle.tag.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import java.net.URI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsView(
    location: String,
    displayedData: DisplayedData?,
    setMode: (DisplayMode) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
    onRefresh: suspend () -> Unit,
) {
    val tags = displayedData?.tags ?: return

    val (urls, nonUrls) = tags.partition {
        try {
            URI.create(it.value!!).toURL()
            return@partition true
        } catch (_: Exception) {
            return@partition false
        }
    }

    Text(text = "URLs", fontWeight = FontWeight.Bold)
    FlowRow {
        urls.forEach {
            TagBox(
                tag = it,
                isUrl = true,
                setMode = setMode,
                location = location,
                onRefresh = onRefresh,
                setDisplayedData = setDisplayedData
            )
        }
    }

    Text(text = "Tags", fontWeight = FontWeight.Bold)
    FlowRow {
        nonUrls.forEach {
            TagBox(
                tag = it,
                setMode = setMode,
                location = location,
                onRefresh = onRefresh,
                setDisplayedData = setDisplayedData
            )
        }
    }
}