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
import androidx.compose.ui.graphics.Color
import com.graphle.common.addFile
import com.graphle.common.supervisorIoScope
import kotlinx.coroutines.launch

object AddFileDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    @Composable
    operator fun invoke(onConfirmed: suspend () -> Unit) {
        if (!isShown) return
        var filename by remember { mutableStateOf("") }

        var showFileNameMissingError by mutableStateOf(false)
        var hasInteractedWithFileName by mutableStateOf(false)

        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Add new file") },
            text = {
                Column {
                    if (hasInteractedWithFileName && showFileNameMissingError) {
                        Text(
                            text = "Name field is required",
                            color = Color.Companion.Red,
                        )
                    }
                    OutlinedTextField(
                        value = filename,
                        onValueChange = {
                            filename = it
                            showFileNameMissingError = filename.isBlank()
                        },
                        isError = hasInteractedWithFileName && showFileNameMissingError,
                        modifier = Modifier.Companion.onFocusChanged {
                            showFileNameMissingError = filename.isBlank()
                            if (it.isFocused) {
                                hasInteractedWithFileName = true
                            } else {
                                showFileNameMissingError = filename.isBlank()
                            }
                        },
                        label = { Text("Filename") },
                        singleLine = true
                    )
                }
            },
            confirmButton =
                {
                    Button(
                        enabled = filename.isNotBlank(),
                        onClick = {
                            val separator =
                                if (location[0] == '/') '/' else '\\' // OS using forward slashes have the root /
                            supervisorIoScope.launch {
                                addFile("$location$separator$filename")
                                onConfirmed()
                            }
                            isShown = false
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