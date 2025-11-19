package com.graphle.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Dialog for displaying error messages to the user.
 */
object ErrorMessage {
    private var showErrorMessage by mutableStateOf( false )
    private var errorMessage by mutableStateOf("")

    /**
     * Sets the error message state.
     *
     * @param showErrorMessage Whether to show the error dialog
     * @param errorMessage The error message text to display
     */
    fun set(
        showErrorMessage: Boolean,
        errorMessage: String,
    ) {
        this.showErrorMessage = showErrorMessage
        this.errorMessage = errorMessage
    }

    /**
     * Renders the error message dialog.
     */
    @Composable
    operator fun invoke() {
        if (!showErrorMessage) return
        AlertDialog(
            onDismissRequest = { showErrorMessage = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorMessage = false
                }) {
                    Text("OK")
                }
            }
        )    }
}