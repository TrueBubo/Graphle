package com.graphle.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ErrorMessage {
    private var showErrorMessage by mutableStateOf( false )
    private var errorMessage by mutableStateOf("")

    fun set(
        showErrorMessage: Boolean,
        errorMessage: String,
    ) {
        this.showErrorMessage = showErrorMessage
        this.errorMessage = errorMessage
    }

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