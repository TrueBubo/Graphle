package com.graphle.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Dialog for displaying invalid file error messages.
 */
object InvalidFileMessage {
    var showInvalidFileMessage by mutableStateOf( true )

    /**
     * Renders the invalid file message dialog.
     *
     * @param location Path to the file that could not be found
     * @param isInvalidFile Whether the file is invalid
     */
    @Composable
    operator fun invoke(
        location: String,
        isInvalidFile: Boolean,
    ) {
        if (!showInvalidFileMessage || !isInvalidFile) return
        AlertDialog(
            onDismissRequest = { showInvalidFileMessage = false },
            title = { Text("Error") },
            text = { Text("Could not find the file at $location") },
            confirmButton = {
                TextButton(onClick = {
                    showInvalidFileMessage = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}
