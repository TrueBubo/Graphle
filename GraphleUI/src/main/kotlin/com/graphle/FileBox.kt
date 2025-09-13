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
                    println("Clicked ${connection.to}")
                    coroutineScope.launch {
                        fetchFilesByLocation(
                            location = connection.to,
                            onLoading = onLoading,
                            onResult = onResult
                        )
                    }
                },
                onRightClick = {
                    println("Right Clicked ${connection.to}")
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

private fun openFile(file: File) {
    if (!file.exists()) {
        return
    }

    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file)
        } else {
            println("OPEN action is not supported on this platform")
        }
    } else {
        println("Desktop API is not supported")
    }
}