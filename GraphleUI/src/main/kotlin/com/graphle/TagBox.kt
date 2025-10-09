package com.graphle

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
import kotlinx.coroutines.launch
import java.net.URI

private fun textsForTag(tag: Tag): List<String> = buildList {
    add(tag.name)
    tag.value?.let { add(it) }
}

@Composable
fun TagBox(
    location: String,
    tag: Tag,
    setMode: (DisplayMode) -> Unit,
    setDisplayedData: (DisplayedData) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    onRefresh: suspend () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.padding(bottom = 10.dp)) {
        if (tag.name.lowercase() == "url") {
            var violatesURLSpec = false
            try {
                URI.create(tag.value!!).toURL()
            } catch (_: Exception) {
                violatesURLSpec = true
                Pill(
                    texts = textsForTag(tag),
                    onRightClick = {
                        showMenu = true
                    }
                )
            }
            if (!violatesURLSpec)
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
            onClick = { println("Clicked $tag") },
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
                        setDisplayedData(DisplayedData(filesWithTag = filesByTag))
                        setMode(DisplayMode.FilesWithTag)
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