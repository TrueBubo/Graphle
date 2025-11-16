package com.graphle.tag.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.graphle.common.filesByTag
import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings
import com.graphle.common.removeTag
import com.graphle.common.supervisorIoScope
import com.graphle.common.ui.Pill
import com.graphle.dialogs.ErrorMessage
import com.graphle.dsl.DSLHistory
import com.graphle.tag.model.Tag
import kotlinx.coroutines.launch

private fun textsForTag(tag: Tag): List<String> = buildList {
    add(tag.name)
    tag.value?.let { add(it) }
}

private fun getTagsCommand(tagName: String) = "find (tagName: \"$tagName\")"

@Composable
internal fun TagBox(
    location: String,
    tag: Tag,
    isUrl: Boolean = false,
    setDisplayedSettings: (DisplayedSettings) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    onRefresh: suspend () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.padding(bottom = 10.dp)) {
        if (isUrl) {
            Pill(
                texts = textsForTag(tag),
                contentColor = MaterialTheme.colors.onPrimary,
                background = MaterialTheme.colors.primary,
                onClick = {
                    uriHandler.openUri(tag.value!!)
                },
                onRightClick = {
                    showMenu = true
                }
            )
        } else Pill(
            texts = textsForTag(tag),
            onRightClick = {
                showMenu = true
            }
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                content = { Text("Open") },
                onClick = {
                    supervisorIoScope.launch {
                        val filesByTag = filesByTag(tag.name)
                        if (filesByTag == null) {
                            ErrorMessage.set(
                                showErrorMessage = true,
                                errorMessage = "Could not load files for tag ${tag.name}"
                            )
                            return@launch
                        }
                        setDisplayedSettings(
                            DisplayedSettings(
                                data = DisplayedData(filesWithTag = filesByTag),
                                mode = DisplayMode.FilesWithTag
                            )
                        )
                        DSLHistory.lastDisplayedCommand.value = getTagsCommand(tag.name)
                    }
                    showMenu = false
                },
            )
            DropdownMenuItem(
                content = { Text("Delete") },
                onClick = {
                    supervisorIoScope.launch {
                        removeTag(location = location, tag = tag)
                        onRefresh()
                    }
                    showMenu = false
                }
            )
        }
    }
}