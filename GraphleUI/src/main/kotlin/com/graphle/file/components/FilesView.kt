package com.graphle.file.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.font.FontWeight
import com.graphle.common.model.DisplayedData
import com.graphle.common.supervisorIoScope
import com.graphle.dialogs.ErrorMessage
import com.graphle.file.util.FileFetcher
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FilesView(
    displayedData: DisplayedData?,
    setLocation: (String) -> Unit,
    setDisplayedData: (DisplayedData?) -> Unit,
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
                            if (it == null) {
                                ErrorMessage.set(
                                    showErrorMessage = true,
                                    errorMessage = "Failed to load ${connection.to}, " +
                                            "check the file exists and you have permission to read it.",
                                )
                            } else {
                                setLocation(connection.to)
                                setDisplayedData(it)
                            }
                        },
                        onRefresh = {
                            supervisorIoScope.launch {
                                FileFetcher.fetch(
                                    location = connection.from,
                                    onResult = { displayedInfo ->
                                        if (displayedInfo == null) {
                                            ErrorMessage.set(
                                                showErrorMessage = true,
                                                errorMessage = "Failed to load ${connection.from}, " +
                                                        "check the file exists and you have permission to read it.",
                                            )
                                        } else {
                                            setDisplayedData(
                                                DisplayedData(
                                                    tags = displayedInfo.tags,
                                                    connections = displayedInfo.connections
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        },
                    )
                }
            }
        }
}
