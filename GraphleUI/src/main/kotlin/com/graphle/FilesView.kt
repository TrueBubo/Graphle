package com.graphle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilesView(
    displayedData: DisplayedData?,
    showHiddenFiles: Boolean,
    onLoading: (Boolean) -> Unit,
    setLocation: (String) -> Unit,
    setDisplayedInfo: (DisplayedData?) -> Unit,
    coroutineScope: CoroutineScope,
) {
    displayedData?.connections
        ?.apply { Text(text = "Files", fontWeight = FontWeight.Bold) }
        ?.let { connections ->
            if (connections.isEmpty()) return@let emptyList<Connection>()
            val connectionsMap = connections.groupBy { it.name }
            buildList {
                connectionsMap["parent"]?.let { addAll(it) }
                connectionsMap.forEach { (key, value) ->
                    if (key != "parent" && key != "descendant") addAll(value)
                }
                connectionsMap["descendant"]?.let { addAll(it) }
            }
        }?.let { connections ->
            Column {
                connections.forEach { connection ->
                    FileBox(
                        connection = connection,
                        showHiddenFiles = showHiddenFiles,
                        onLoading = onLoading,
                        onResult = {
                            setLocation(connection.to)
                            setDisplayedInfo(it)
                        },
                        onRefresh = {
                            coroutineScope.launch {
                                fetchFilesByLocation(
                                    location = connection.to,
                                    showHiddenFiles = showHiddenFiles,
                                    onLoading = onLoading,
                                    onResult = { displayedInfo ->
                                        setDisplayedInfo(
                                            DisplayedData(
                                                tags = displayedInfo?.tags ?: emptyList(),
                                                connections = displayedInfo?.connections ?: emptyList(
                                                )
                                            )
                                        )
                                    })
                            }
                        },
                        coroutineScope = coroutineScope
                    )
                }
            }
        }
}
