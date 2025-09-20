package com.graphle

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TagTextField(
    value: String,
    onValueChange: (String) -> Unit,
    location: String,
    tagName: String,
    tagValue: String,
    tagNameSetter: (String) -> Unit,
    tagValueSetter: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.Companion.onPreviewKeyEvent { event ->
            handleTagFieldKeyEvent(
                event = event,
                location = location,
                tagName = tagName,
                tagValue = tagValue,
                tagNameSetter = tagNameSetter,
                tagValueSetter = tagValueSetter,
            )
        }
    )
}

fun handleTagFieldKeyEvent(
    event: KeyEvent,
    location: String,
    tagName: String,
    tagValue: String,
    tagNameSetter: (String) -> Unit,
    tagValueSetter: (String) -> Unit,
): Boolean =
    if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
        supervisorIoScope.launch {
            if (tagName == "") return@launch
            if (tagValue != "") apolloClient.addTagToFile(
                location,
                tagName,
                tagValue
            )
            else apolloClient.addTagToFile(location, tagName)
        }

        tagNameSetter("")
        tagValueSetter("")
        true
    } else false

