package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import kotlin.io.path.Path

private fun pillText(relationshipName: String, value: String?): String =
    "${
        when (relationshipName) {
            "descendant" -> "⬇"
            "parent" -> "⬆"
            else -> relationshipName
        }
    }${value?.let { " : ($it)" } ?: ""}"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBox(
    connection: Connection,
    showHiddenFiles: Boolean,
    onLoading: (Boolean) -> Unit,
    onResult: (DisplayedData?) -> Unit,
    onRefresh: () -> Unit,
    coroutineScope: CoroutineScope
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.padding(bottom = 10.dp)) {
        Row {
            Pill(
                texts = listOf(
                    pillText(relationshipName = connection.name, value = connection.value),
                    connection.to
                ),
                onClick = {
                    coroutineScope.launch {
                        fetchFilesByLocation(
                            location = connection.to,
                            showHiddenFiles = showHiddenFiles,
                            onLoading = onLoading,
                            onResult = onResult
                        )
                    }
                },
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
                        openFile(Path(connection.to).toFile())
                        showMenu = false
                    },
                )
                DropdownMenuItem(
                    content = { Text("Delete") },
                    onClick = {
                        coroutineScope.launch {
                            apolloClient.removeFileByLocation(connection.to)
                            onRefresh()
                        }
                        showMenu = false
                    }
                )
            }
        }
    }
}

