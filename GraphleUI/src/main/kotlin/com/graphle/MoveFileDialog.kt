package com.graphle

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
import com.graphle.type.FileType
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object MoveFileDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    @Composable
    operator fun invoke(onMoved: suspend () -> Unit) {
        if (!isShown) return
        var to by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Moving file") },
            text = {
                Column {
                    OutlinedTextField(
                        value = to,
                        onValueChange = { to = it },
                        label = { Text("Move to") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
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