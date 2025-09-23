package com.graphle

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
import androidx.compose.ui.unit.dp
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
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Enter information about the tag") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tag name") },
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
                        isShown = false
                        supervisorIoScope.launch {
                            if (name == "") return@launch
                            Tag(name = name, value = value.ifBlank { null }).save(location = location)
                            onSubmitted()
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