package com.graphle

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import java.awt.datatransfer.StringSelection
import kotlin.io.path.Path

@Composable
fun FileMenu(
    location: String,
    setShowMenu: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    DropdownMenuItem(
        content = { Text("Open") },
        onClick = {
            openFile(Path(location).toFile())
            setShowMenu(false)
        },
    )

    DropdownMenuItem(
        content = { Text("Copy path") },
        onClick = {
            clipboard.setContents(StringSelection(location), null)
            setShowMenu(false)
        }
    )

    DropdownMenuItem(
        content = { Text("Add tag") },
        onClick = {
            AddTagDialog.set(location = location, isShown = true)
            setShowMenu(false)
        }
    )

    DropdownMenuItem(
        content = { Text("Add relationship") },
        onClick = {
            AddRelationshipDialog.set(location = location, isShown = true)
            setShowMenu(false)
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