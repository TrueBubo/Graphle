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
fun RelationshipBox(
    relationshipName: String,
    location: String,
    onLoading: (Boolean) -> Unit,
    onResult: (List<Connection>?) -> Unit,
    onRefresh: () -> Unit,
    coroutineScope: CoroutineScope
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Text(
            text = relationshipName,
            modifier = Modifier.onClick(onClick = {
                println("Clicked $relationshipName")
                coroutineScope.launch {
                    fetchFilesFromFileByRelationship(
                        fromLocation = location,
                        relationshipName = relationshipName,
                        onLoading = onLoading,
                        onResult = onResult
                    )
                }
            }).onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                onClick = {
                    println("Right Clicked $relationshipName")
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
                        apolloClient.removeFileByLocation(location)
                        onRefresh()
                    }
                    showMenu = false
                }
            )
        }
    }
}