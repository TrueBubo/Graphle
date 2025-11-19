package com.graphle.tag.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.graphle.common.model.DisplayedSettings
import java.net.URI

/**
 * Displays tags for a file, separating URLs from regular tags.
 *
 * @param location File location
 * @param displayedSettings Current displayed settings
 * @param setDisplayedSettings Callback to update displayed settings
 * @param onRefresh Callback to refresh the view
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsView(
    location: String,
    displayedSettings: DisplayedSettings,
    setDisplayedSettings: (DisplayedSettings) -> Unit,
    onRefresh: suspend () -> Unit,
) {
    val tags = displayedSettings.data?.tags ?: return

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
                setDisplayedSettings = setDisplayedSettings,
                location = location,
                onRefresh = onRefresh,
            )
        }
    }

    Text(text = "Tags", fontWeight = FontWeight.Bold)
    FlowRow {
        nonUrls.forEach {
            TagBox(
                tag = it,
                setDisplayedSettings = setDisplayedSettings,
                location = location,
                onRefresh = onRefresh,
            )
        }
    }
}