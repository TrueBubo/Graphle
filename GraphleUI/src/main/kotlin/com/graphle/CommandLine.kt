package com.graphle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        onDismissRequest = {showDialog = false},
        title = { Text("Error") },
        text = { Text("Failed to connect to the autocompleter.") },
        confirmButton = {
            TextButton(onClick = {showDialog = false}) {
                Text("OK")
            }
        }
    )
}

val fieldHeight = 56.dp

@Composable
fun TopBar(
    location: String
) {
    var dslValue by remember { mutableStateOf("") }
    var dslCommand by remember { mutableStateOf("") }
    var showAppMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                onClick = {
                    println("Show menu")
                    showAppMenu = true
                },
                modifier = Modifier.height(fieldHeight)
            ) {
                Text("☰")
            }
            DropdownMenu(expanded = showAppMenu, onDismissRequest = { showAppMenu = false }) {
                DropdownMenuItem(
                    content = { Text("Open") },
                    onClick = {
                        showAppMenu = false
                        openFile(Path(location).toFile())
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
            coroutineScope.launch {
                DSLWebSocketManager.sendAutocompleteRequest(dslCommand)
            }
        }
    ) { Text("DSL") }

    WebSocketFailedPopUp()
}