package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.onClick
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBox(
    filename: String,
    onLoading: (Boolean) -> Unit,
    onResult: (Map<PropertyType, List<String>>?) -> Unit,
    onRefresh: () -> Unit,
    coroutineScope: CoroutineScope
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Text(
            text = filename,
            modifier = Modifier.onClick(
                onClick = {
                    println("Clicked $filename")
                    coroutineScope.launch {
                        fetchFilesByLocation(
                            location = filename,
                            onLoading = onLoading,
                            onResult = onResult
                        )
                    }
                }
            ).onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                onClick = {
                    println("Right Clicked ${filename}Name")
                    showMenu = true
                }
            )
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                content = { Text("Open") },
                onClick = {
                    showMenu = false
                },
            )
            DropdownMenuItem(
                content = { Text("Delete") },
                onClick = {
                    coroutineScope.launch {
                        apolloClient.removeFileByLocation(filename)
                        onRefresh()
                    }
                    showMenu = false
                }
            )
        }
    }
}