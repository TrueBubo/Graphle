package com.graphle

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun CommandLine() {
    var dslValue by remember { mutableStateOf("") }
    var dslCommand by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        println("Launched effect")
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
}