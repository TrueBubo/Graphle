package com.graphle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
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

private fun processSelectSuggestion(
    event: KeyEvent,
    selectedIndex: Int,
    setSelectedIndex: (Int) -> Unit,
    autocompleteList: List<String>,
    setDslCommand: (String) -> Unit,
    setAreSuggestionsShown: (Boolean) -> Unit
): Boolean = when {
    event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
        setSelectedIndex((selectedIndex + 1).coerceAtMost(autocompleteList.lastIndex))
        true
    }

    event.type == KeyEventType.KeyDown && !event.isShiftPressed && event.key == Key.Tab -> {
        setSelectedIndex((selectedIndex + 1).coerceAtMost(autocompleteList.lastIndex))
        true
    }

    event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
        setSelectedIndex((selectedIndex - 1).coerceAtLeast(0))
        true
    }

    event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.Tab -> {
        setSelectedIndex((selectedIndex - 1).coerceAtMost(autocompleteList.lastIndex))
        true
    }

    event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
        if (selectedIndex in autocompleteList.indices) {
            setDslCommand(autocompleteList[selectedIndex])
            setAreSuggestionsShown(false)
            setSelectedIndex(-1)
            true
        } else {
            false
        }
    }

    else -> false
}


val fieldHeight = 56.dp

@Composable
fun TopBar(
    location: String,
    setLocation: (String) -> Unit,
    onResult: (DisplayedData?) -> Unit,
    setDarkMode: (Boolean) -> Unit,
    getDarkMode: () -> Boolean,
) {
    var autocompleteList by remember { mutableStateOf<List<String>>(emptyList()) }
    var areSuggestionsShown by remember { mutableStateOf(false) }
    var dslCommand by remember { mutableStateOf("") }
    var showAppMenu by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        DSLWebSocketManager.connect()

        // Listen for autocomplete messages
        DSLWebSocketManager.messages.collect { list ->
            autocompleteList = list
            areSuggestionsShown = autocompleteList.isNotEmpty() && dslCommand.isNotEmpty()
        }
    }

    Row {
        AppMenu(
            showAppMenu = showAppMenu,
            setShowAppMenu = { showAppMenu = it },
            onResult = onResult,
            location = location,
            setLocation = setLocation,
            setDarkMode = setDarkMode,
            getDarkMode = getDarkMode,
        )
        Box {
            Column {
                TextField(
                    value = dslCommand,
                    onValueChange = {
                        dslCommand = it
                        supervisorIoScope.launch {
                            DSLWebSocketManager.sendAutocompleteRequest(dslCommand)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .onPreviewKeyEvent { event ->
                            processSelectSuggestion(
                                event = event,
                                selectedIndex = selectedIndex,
                                setSelectedIndex = { selectedIndex = it },
                                autocompleteList = autocompleteList,
                                setDslCommand = { dslCommand = it },
                                setAreSuggestionsShown = { areSuggestionsShown = it }
                            )
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                    )
                )

            }
        }
    }

    if (areSuggestionsShown) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface)
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
            ) {
                autocompleteList.forEachIndexed { index, suggestion ->
                    val isSelected = index == selectedIndex
                    DropdownMenuItem(
                        onClick = {
                            dslCommand = suggestion
                            areSuggestionsShown = false
                            selectedIndex = -1
                        }
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colors.primaryVariant else Color.Transparent
                                )
                                .padding(8.dp),
                        )
                    }
                }
            }
        }
    }

    WebSocketFailedPopUp()
}