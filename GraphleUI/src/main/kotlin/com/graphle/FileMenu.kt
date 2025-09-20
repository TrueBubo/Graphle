package com.graphle

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import kotlin.io.path.Path

@Composable
fun FileMenu(
    location: String,
    setShowMenu: (Boolean) -> Unit,
    onRefresh: () -> Unit) {
    DropdownMenuItem(
        content = { Text("Open") },
        onClick = {
            openFile(Path(location).toFile())
            setShowMenu(false)
        },
    )

    DropdownMenuItem(
        onClick = {
            AddTagDialog.location = location
            AddTagDialog.isShown = true
            setShowMenu(false)
        },
        content = {
            Text("Add tag")
        }
    )

    DropdownMenuItem(
        content = { Text("Move to trash") },
        onClick = {
            setShowMenu(false)
            supervisorIoScope.launch {
                Trash.moveToTrash(Path(location))
                onRefresh()
            }
        }
    )

    DropdownMenuItem(
        content = { Text("Delete permanently") },
        onClick = {
            setShowMenu(false)
            supervisorIoScope.launch {
                apolloClient.removeFileByLocation(location)
                onRefresh()
            }
        }
    )
}