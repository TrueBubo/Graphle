package com.graphle.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.graphle.common.apolloClient
import com.graphle.common.removeFileByLocation
import com.graphle.common.supervisorIoScope
import kotlinx.coroutines.launch

/**
 * Dialog for confirming permanent file deletion.
 */
object DeleteFileDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    /**
     * Sets the dialog state and file location.
     *
     * @param location Path to the file to delete
     * @param isShown Whether the dialog should be shown
     */
    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    /**
     * Renders the delete file confirmation dialog.
     *
     * @param onConfirmed Callback invoked after successful deletion
     */
    @Composable
    operator fun invoke(onConfirmed: suspend () -> Unit) {
        if (!isShown) return
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Permanently delete $location") },
            text = {
                Text("Are you sure you want to permanently delete $location")

            },
            confirmButton = {
                Button(
                    onClick = {
                        println("Deleted file $location")
                        isShown = false
                        supervisorIoScope.launch {
                            apolloClient.removeFileByLocation(location)
                            onConfirmed()
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton =
                {
                    Button(onClick = { isShown = false }) {
                        Text("Cancel")
                    }
                }
        )
    }
}