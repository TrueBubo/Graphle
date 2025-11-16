package com.graphle.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.graphle.tag.model.Tag
import com.graphle.common.supervisorIoScope
import kotlinx.coroutines.launch

object AddTagDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)

    fun set(
        location: String,
        isShown: Boolean,
    ) {
        this.location = location
        this.isShown = isShown
    }

    @Composable
    operator fun invoke(onSubmitted: suspend () -> Unit)
    {
        if (!isShown) return
        var name by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var showNameMissingError by remember { mutableStateOf(false) }
        var hasInteractedWithName by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Enter information about the tag") },
            text = {
                Column {
                    if (hasInteractedWithName && showNameMissingError) {
                        Text(
                            text = "Name field is required",
                            color = Color.Red,
                        )
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showNameMissingError = name.isBlank()
                        },
                        isError = hasInteractedWithName && showNameMissingError,
                        modifier = Modifier.onFocusChanged {
                            showNameMissingError = name.isBlank()
                            if (it.isFocused) {
                                hasInteractedWithName = true
                            } else {
                                showNameMissingError = name.isBlank()
                            }
                        },
                        label = { Text("Tag name*") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Tag value") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        supervisorIoScope.launch {
                            Tag(name = name, value = value.ifBlank { null }).save(location = location)
                            onSubmitted()
                        }
                        isShown = false
                    },
                    enabled = name.isNotBlank()
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