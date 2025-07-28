package com.graphle

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
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
import kotlinx.coroutines.launch

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

@Composable
fun CommandLine() {
    var dslValue by remember { mutableStateOf("") }
    var dslCommand by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        DSLWebSocketManager.connect()

        // Listen for autocomplete messages
        DSLWebSocketManager.messages.collect { list ->
            dslValue = list.joinToString("\n")
        }
    }

    Text(dslValue)
    TextField(
        value = dslCommand,
        onValueChange = { dslCommand = it },
        singleLine = true
    )

    Button(
        onClick = {
            coroutineScope.launch {
                DSLWebSocketManager.sendAutocompleteRequest(dslCommand)
            }
        }
    ) { Text("DSL") }

    WebSocketFailedPopUp()
}