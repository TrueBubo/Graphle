package com.graphle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

object AddRelationshipDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)
    private var isBidirectional by mutableStateOf(false)

    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    @Composable
    operator fun invoke(onSubmitted: suspend () -> Unit, onUpdatedData: () -> DisplayedData?) {
        if (!isShown) return
        var to by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Enter information about the relationship") },
            text = {
                Column {
                    OutlinedTextField(
                        value = to,
                        onValueChange = { to = it },
                        label = { Text("Related to") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Relationship name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Relationship value") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Is bidirectional")
                        Spacer(Modifier.width(8.dp))
                        Checkbox(
                            checked = isBidirectional,
                            onCheckedChange = { isBidirectional = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isShown = false
                        supervisorIoScope.launch {
                            if (location == "" || name == "") return@launch
                            Connection(
                                from = location,
                                to = to,
                                name = name,
                                value = value.ifBlank { null }).also { println(it) }.save(isBidirectional)
                            onSubmitted()
                            onUpdatedData()?.connections.orEmpty()
                                .none { it.to == to }
                                .also {
                                    if (it) {
                                        ErrorMessage.showErrorMessage = true
                                        ErrorMessage.errorMessage = "Could not create a relationship with $to"
                                    }
                                }
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