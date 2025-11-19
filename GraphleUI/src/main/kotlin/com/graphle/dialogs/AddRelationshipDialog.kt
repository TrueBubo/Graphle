package com.graphle.dialogs

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.graphle.file.model.Connection
import com.graphle.common.model.DisplayedData
import com.graphle.common.supervisorIoScope
import kotlinx.coroutines.launch

/**
 * Dialog for adding relationships/connections between files.
 */
object AddRelationshipDialog {
    private var location by mutableStateOf("")
    private var isShown by mutableStateOf(false)
    private var isBidirectional by mutableStateOf(false)

    /**
     * Sets the dialog state and source file location.
     *
     * @param location Path to the source file
     * @param isShown Whether the dialog should be shown
     */
    fun set(location: String, isShown: Boolean) {
        this.location = location
        this.isShown = isShown
    }

    /**
     * Renders the add relationship dialog.
     *
     * @param onSubmitted Callback invoked after successful relationship creation
     * @param onUpdatedData Function providing the current displayed data
     */
    @Composable
    operator fun invoke(onSubmitted: suspend () -> Unit, onUpdatedData: () -> DisplayedData?) {
        var showToMissingError by mutableStateOf(false)
        var hasInteractedWithTo by mutableStateOf(false)

        var showNameMissingError by mutableStateOf(false)
        var hasInteractedWithName by mutableStateOf(false)

        if (!isShown) return
        var to by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isShown = false },
            title = { Text("Enter information about the relationship") },
            text = {
                Column {
                    if (showToMissingError && hasInteractedWithTo) {
                        Text(
                            text = "Related to field is required",
                            color = Color.Red,
                        )
                    }
                    OutlinedTextField(
                        value = to,
                        onValueChange = {
                            to = it
                            showToMissingError = to.isBlank()
                        },
                        label = { Text("Related to*") },
                        isError = hasInteractedWithTo && showToMissingError,
                        modifier = Modifier.onFocusChanged {
                            showToMissingError = to.isBlank()
                            if (it.isFocused) {
                                hasInteractedWithTo = true
                            } else {
                                showToMissingError = to.isBlank()
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showNameMissingError && hasInteractedWithName) {
                        Text(
                            text = "Name field is required",
                            color = Color.Red,
                        )
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            showNameMissingError = name.isBlank()
                            name = it
                        },
                        label = { Text("Relationship name*") },
                        isError = showNameMissingError && hasInteractedWithName,
                        modifier = Modifier.onFocusChanged {
                            showNameMissingError = name.isBlank()
                            if (it.isFocused) {
                                hasInteractedWithName = true
                            } else {
                                showNameMissingError = name.isBlank()
                            }
                        },
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
                    enabled = name.isNotEmpty() && to.isNotEmpty(),
                    onClick = {
                        isShown = false
                        supervisorIoScope.launch {
                            Connection(
                                from = location,
                                to = to,
                                name = name,
                                value = value.ifBlank { null }).save(isBidirectional)
                            onSubmitted()
                            onUpdatedData()?.connections.orEmpty()
                                .none { it.to == to }
                                .also {
                                    if (it) {
                                        ErrorMessage.set(
                                            showErrorMessage = true,
                                            errorMessage = "Could not create a relationship with $to"
                                        )
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