package com.graphle

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ErrorMessage {
    var showErrorMessage by mutableStateOf( false )
    var errorMessage by mutableStateOf("")

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