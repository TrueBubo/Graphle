package com.graphle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FilesView(
    displayedData: DisplayedData?,
    onLoading: (Boolean) -> Unit,
    setLocation: (String) -> Unit,
    setDisplayedInfo: (DisplayedData?) -> Unit,
    coroutineScope: CoroutineScope,
) {
    displayedData?.connections
        ?.apply { Text(text = "Files", fontWeight = FontWeight.Bold) }
        ?.let { connections ->
            if (connections.isEmpty()) return@let emptyList<Connection>()
            println(connections)
            val connectionsMap = connections.groupBy { it.name }
            buildList {
                connectionsMap["parent"]?.let { addAll(it) }
                connectionsMap.forEach { (key, value) ->
                    if (key != "parent" && key != "descendant") addAll(value)
                }
                connectionsMap["descendant"]?.let { addAll(it) }
            }
        }
        ?.let { connections ->
            println(connections)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = connections, key = { it }) { connection ->
                    FileBox(
                        connection = connection,
                        onLoading = onLoading,
                        onResult = {
                            setLocation(connection.to)
                            setDisplayedInfo(it)
                        },
                        onRefresh = {
                            coroutineScope.launch {
                                fetchFilesByLocation(
                                    location = connection.to,
                                    onLoading = onLoading,
                                    onResult = { displayedInfo ->
                                        setDisplayedInfo(
                                            DisplayedData(
                                                tags = displayedInfo?.tags ?: emptyList(),
                                                connections = displayedInfo?.connections ?: emptyList(
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        },
                        coroutineScope = coroutineScope
                    )
                }
            }
        }
}