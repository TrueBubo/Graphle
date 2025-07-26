package com.graphle

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun CommandLine() {
    var dslValue by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Text(dslValue)

    Button(
        onClick = {
            coroutineScope.launch {
                dslAutoCompleter(autoCompleterClient) {dslValue = it}
            }
        }
    ) { Text("DSL") }
}