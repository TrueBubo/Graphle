package com.graphle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
                    supervisorIoScope.launch {
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
                FileMenu(
                    location = connection.to,
                    setShowMenu = { showMenu = it },
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

