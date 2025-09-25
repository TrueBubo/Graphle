package com.graphle

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object InvalidFileMessage {
    var showInvalidFileMessage by mutableStateOf( true )

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
