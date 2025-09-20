package com.graphle

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilesView(
    displayedData: DisplayedData?,
    setLocation: (String) -> Unit,
    setDisplayedInfo: (DisplayedData?) -> Unit,
) {
    displayedData?.connections
        ?.apply { Text(text = "Files", fontWeight = FontWeight.Bold) }
        ?.let { connections ->
            if (connections.isEmpty()) return@let emptyList()
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
                        onResult = {
                            setLocation(connection.to)
                            setDisplayedInfo(it)
                        },
                        onRefresh = {
                            supervisorIoScope.launch {
                                FileFetcher.fetch(
                                    location = connection.from,
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
                    )
                }
            }
        }
}
