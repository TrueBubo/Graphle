package com.graphle

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.graphle.type.FileType
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlin.io.path.Path

@Composable
fun FileMenu(
    location: String,
    setShowMenu: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    val fileType = supervisorIoScope.async { fileType(location) }
    if (config.server.localhost || runBlocking { fileType.await() == FileType.File }) {
        DropdownMenuItem(
            content = { Text("Open") },
            onClick = {
                if (config.server.localhost) openFile(Path(location).toFile())
                else {
                    supervisorIoScope.launch {
                        val downloadPath = "${userHome}${File.separator}GraphleDownloads${File.separator}$location"
                        downloadFile(location, createParentDirectories(downloadPath))
                            .also { downloadSuccessful ->
                                if (!downloadSuccessful) ErrorMessage.set(
                                    showErrorMessage = true,
                                    errorMessage = "Could not download the file $location, " +
                                            "check whether it is not a directory or if you have necessary permissions set"
                                )
                                else openFile(Path(downloadPath).toFile())
                            }
                    }
                }
                setShowMenu(false)
            },
        )
    }

    if (runBlocking { fileType.await() == FileType.Directory}) {
        DropdownMenuItem(
            content = { Text("Add file") },
            onClick = {
                AddFileDialog.set(
                    location = location,
                    isShown = true
                )
                setShowMenu(false)
            }
        )
    }

    DropdownMenuItem(
        content = { Text("Copy path") },
        onClick = {
            clipboard.setContents(StringSelection(location), null)
            setShowMenu(false)
        }
    )

    DropdownMenuItem(
        content = { Text("Move") },
        onClick = {
            MoveFileDialog.set(location = location, isShown = true)
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
            DeleteFileDialog.set(location = location, isShown = true)
        }
    )
}