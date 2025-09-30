package com.graphle

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

object DeleteFileDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

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