package com.graphle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.HorizontalRuler
import androidx.compose.ui.layout.VerticalRuler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.io.path.Path

@Composable
private fun WebSocketFailedPopUp() {
    var showDialog by remember { mutableStateOf(false) }
    val isFailed by DSLWebSocketManager.isFailed.collectAsState()
    var failureHandled by remember { mutableStateOf(false) }

    LaunchedEffect(isFailed) {
        if (isFailed && !failureHandled) {
            showDialog = true
            failureHandled = true
        }
    }

    if (!showDialog) return
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Error") },
        text = { Text("Failed to connect to the autocompleter.") },
        confirmButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("OK")
            }
        }
    )
}

val fieldHeight = 56.dp

@Composable
fun TopBar(
    location: String,
    showHiddenFiles: Boolean,
    onLoading: (Boolean) -> Unit,
    onResult: (DisplayedData?) -> Unit,
    setShowHiddenFiles: (Boolean) -> Unit,
) {
    var dslValue by remember { mutableStateOf("") }
    var dslCommand by remember { mutableStateOf("") }
    var showAppMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        DSLWebSocketManager.connect()

        // Listen for autocomplete messages
        DSLWebSocketManager.messages.collect { list ->
            dslValue = list.joinToString("\n")
        }
    }

    Text(dslValue)
    Row {
        Box {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                onClick = {
                    println("Show menu")
                    showAppMenu = true
                },
                modifier = Modifier.height(fieldHeight)
            ) {
                Text("☰")
            }

            DropdownMenu(expanded = showAppMenu, onDismissRequest = { showAppMenu = false }) {
                FileMenu(
                    location = location,
                    setShowMenu = { showAppMenu = it },
                    onRefresh = {
                        supervisorIoScope.launch {
                            fetchFilesByLocation(
                                location = location,
                                showHiddenFiles = showHiddenFiles,
                                onLoading = onLoading,
                                onResult = { displayedInfo ->
                                    onResult(
                                        DisplayedData(
                                            tags = displayedInfo?.tags ?: emptyList(),
                                            connections = displayedInfo?.connections ?: emptyList(
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    }
                )

                Divider()

                DropdownMenuItem(
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Hidden Files")
                            Spacer(Modifier.width(8.dp))
                            Checkbox(
                                checked = showHiddenFiles,
                                onCheckedChange = null
                            )
                        }
                    },
                    onClick = {
                        val newShowHiddenFilesState = !showHiddenFiles
                        setShowHiddenFiles(newShowHiddenFilesState)

                        showAppMenu = false
                        supervisorIoScope.launch {
                            fetchFilesByLocation(
                                location = location,
                                showHiddenFiles = newShowHiddenFilesState,
                                onLoading = onLoading,
                                onResult = onResult
                            )
                        }
                    }
                )
            }
        }
        TextField(
            value = dslCommand,
            onValueChange = { dslCommand = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
        )
    }

    Button(
        onClick = {
            supervisorIoScope.launch {
                DSLWebSocketManager.sendAutocompleteRequest(dslCommand)
            }
        }
    ) { Text("DSL") }

    WebSocketFailedPopUp()
}