package com.graphle.file.components

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
import com.graphle.file.model.Connection
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings
import com.graphle.common.supervisorIoScope
import com.graphle.common.ui.Pill
import com.graphle.file.util.FileFetcher
import kotlinx.coroutines.launch

/**
 * Formats relationship name and value for display in a pill.
 */
private fun pillText(relationshipName: String, value: String?): String =
    "${
        when (relationshipName) {
            "descendant" -> "⬇"
            "parent" -> "⬆"
            else -> relationshipName
        }
    }${value?.let { " = $it" } ?: ""}"

/**
 * Displays a file connection as a clickable pill with a context menu.
 *
 * @param connection The file connection to display
 * @param onResult Callback when the connected file is clicked
 * @param onRefresh Callback to refresh the view
 * @param displayRelationshipInfo Whether to show relationship details in the pill
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileBox(
    connection: Connection,
    onResult: (DisplayedSettings) -> Unit,
    onRefresh: () -> Unit,
    displayRelationshipInfo: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.padding(bottom = 10.dp)) {
        Row {
            Pill(
                texts = buildList {
                    if (displayRelationshipInfo) add(
                        pillText(
                            relationshipName = connection.name,
                            value = connection.value
                        )
                    )
                    add(connection.to)
                },
                onClick = {
                    supervisorIoScope.launch {
                        FileFetcher.fetch(
                            location = connection.to,
                            onResult = onResult,
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
                    connection = connection,
                    setShowMenu = { showMenu = it },
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

