package com.graphle.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.graphle.common.apolloClient
import com.graphle.common.fileType
import com.graphle.common.moveFile
import com.graphle.common.supervisorIoScope
import com.graphle.type.FileType
import kotlinx.coroutines.launch

/**
 * Dialog for moving files to a different directory.
 */
object MoveFileDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    /**
     * Sets the dialog state and file location.
     *
     * @param location Path to the file to move
     * @param isShown Whether the dialog should be shown
     */
    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    /**
     * Renders the move file dialog.
     *
     * @param onMoved Callback invoked after successful file move
     */
    @Composable
    operator fun invoke(onMoved: suspend () -> Unit) {
        var showToMissingError by mutableStateOf(false)
        var hasInteractedWithTo by mutableStateOf(false)

        if (!isShown) return
        var to by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Moving file") },
            text = {
                Column {
                    OutlinedTextField(
                        value = to,
                        onValueChange = {
                            to = it
                            showToMissingError = to.isBlank()
                        },
                        isError = hasInteractedWithTo && showToMissingError,
                        modifier = Modifier.onFocusChanged {
                            showToMissingError = to.isBlank()
                            if (it.isFocused) {
                                hasInteractedWithTo = true
                            } else {
                                showToMissingError = to.isBlank()
                            }
                        },
                        label = { Text("Move to") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = to.isNotBlank(),
                    onClick = {
                        isShown = false
                        supervisorIoScope.launch {
                            if (fileType(to) != FileType.Directory) {
                                ErrorMessage.set(
                                    errorMessage = "Could not move file to $to, can only move to directories",
                                    showErrorMessage = true
                                )
                                return@launch
                            }
                            val separator =
                                if (location[0] == '/') '/' else '\\' // OS using forward slashes have the root /
                            val filename = location.split(separator).last()
                            apolloClient.moveFile(location, "$to$separator$filename")
                            onMoved()
                        }

                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { isShown = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}