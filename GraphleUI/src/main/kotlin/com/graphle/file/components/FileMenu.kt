package com.graphle.file.components

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.graphle.file.model.Connection
import com.graphle.common.Trash
import com.graphle.common.clipboard
import com.graphle.common.config
import com.graphle.common.fileType
import com.graphle.common.removeRelationship
import com.graphle.common.supervisorIoScope
import com.graphle.common.userHome
import com.graphle.dialogs.AddFileDialog
import com.graphle.dialogs.AddRelationshipDialog
import com.graphle.dialogs.AddTagDialog
import com.graphle.dialogs.DeleteFileDialog
import com.graphle.dialogs.ErrorMessage
import com.graphle.dialogs.MoveFileDialog
import com.graphle.file.util.createParentDirectories
import com.graphle.file.util.downloadFile
import com.graphle.file.util.openFile
import com.graphle.type.FileType
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlin.io.path.Path

@Composable
internal fun FileMenu(
    location: String,
    connection: Connection?,
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

    if (connection != null && connection.name !in listOf("descendant", "parent")) {
        DropdownMenuItem(
            content = { Text("Remove relationship") },
            onClick = {
                println("Remove relationship ${connection}")
                supervisorIoScope.launch {
                    removeRelationship(connection)
                    onRefresh()
                }
                setShowMenu(false)
            }
        )
    }

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