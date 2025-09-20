package com.graphle

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsView(
    displayedData: DisplayedData?,
    colors: Colors,
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
            TagBox(tag = it)
        }
    }

    Text(text = "Tags", fontWeight = FontWeight.Bold)
    FlowRow {
        nonUrls.forEach {
            TagBox(tag = it)
        }
    }
}