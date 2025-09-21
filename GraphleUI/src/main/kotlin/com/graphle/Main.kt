package com.graphle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private const val minWidthPx = 600
private const val minHeightPx = 400
fun main() = application {
    var title by remember { mutableStateOf("Graphle") }
    Window(onCloseRequest = ::exitApplication, title = title) {
        window.minimumSize = java.awt.Dimension(minWidthPx, minHeightPx)
        App(setTitle = { title = it })
    }
}
