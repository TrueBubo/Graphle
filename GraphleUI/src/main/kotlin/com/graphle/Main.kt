package com.graphle

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

const val serverURL = "http://localhost:8080/graphql"
val minUpdateDelay = 1000.milliseconds

val apolloClient = ApolloClient.Builder()
    .serverUrl(serverURL)
    .build()

data class DisplayedInfo(
    val tags: List<Tag> = emptyList(),
    val connections: List<Connection> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var location by remember { mutableStateOf("/home") }
    var oldLocation by remember { mutableStateOf("") }
    var lastUpdated by remember { mutableStateOf(0L) }
    var tagName by remember { mutableStateOf("Name") }
    var tagValue by remember { mutableStateOf("Value") }
    var isLoading by remember { mutableStateOf(false) }
    var displayedInfo by remember { mutableStateOf(
        runBlocking {
            fetchFilesByLocation(
                location = location,
                onLoading = { isLoading = it },
                onResult = { }
            )
        }
    ) }
    var showInvalidFileDialog by remember { mutableStateOf(true) }
    val defaultSystemThemeIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(defaultSystemThemeIsDark) }
    val coroutineScope = rememberCoroutineScope()



    MaterialTheme(colors = if (isDarkTheme) DarkColorPalette else LightColorPalette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )

                CommandLine()

                TextField(
                    value = location,
                    onValueChange = { location = it },
                    singleLine = true,
                    modifier = Modifier.onPreviewKeyEvent { event ->
                        val canRefresh = !isLoading && ((location != oldLocation)
                                || (currentTimeMillis() - lastUpdated > minUpdateDelay.inWholeMilliseconds))
                        if (event.key == Key.Enter && canRefresh) {
                            oldLocation = location
                            lastUpdated = currentTimeMillis()
                            coroutineScope.launch {
                                fetchFilesByLocation(
                                    location = location,
                                    onLoading = { isLoading = it },
                                    onResult = { info ->
                                        showInvalidFileDialog = true
                                        displayedInfo = info
                                        println("Info $info")

                                    }
                                )
                            }

                            true

                        } else false
                    }
                )

                TagTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    location = location,
                    tagName = tagName,
                    tagValue = tagValue,
                    tagNameSetter = { tagName = it },
                    tagValueSetter = { tagValue = it },
                    coroutineScope = coroutineScope,
                )

                TagTextField(
                    value = tagValue,
                    onValueChange = { tagValue = it },
                    location = location,
                    tagName = tagName,
                    tagValue = tagValue,
                    tagNameSetter = { tagName = it },
                    tagValueSetter = { tagValue = it },
                    coroutineScope = coroutineScope,
                )

                if (isLoading) {
                    Text("Loading...")
                } else {
                    Text("File:")
                    println(displayedInfo)
                    if (displayedInfo == null) {
                        Text("Could not find the file")
                        if (showInvalidFileDialog) AlertDialog(
                            onDismissRequest = { showInvalidFileDialog = false },
                            title = { Text("Error") },
                            text = { Text("Could not find the file at $location") },
                            confirmButton = {
                                TextButton(onClick = {showInvalidFileDialog = false}) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    else {
                        FilesView(
                            displayedInfo = displayedInfo,
                            onLoading = { isLoading = it },
                            setLocation = { location = it },
                            setDisplayedInfo = {
                                displayedInfo = it
                                showInvalidFileDialog = true
                            },
                            coroutineScope = coroutineScope,
                        )
                        println("Displayed before tags: $displayedInfo")
                        val uriHandler = LocalUriHandler.current
                        displayedInfo?.tags
                            ?.apply { Text(text = "Tags", fontWeight = FontWeight.Bold) }
                            ?.forEach {
                                if (it.name.lowercase() == "url") {
                                    var violatesURLSpec = false
                                    try {
                                        URI.create(it.value!!).toURL()
                                    } catch (_: Exception) {
                                        violatesURLSpec = true
                                        Text(
                                            text = it.name + if (it.value != null) ": ${it.value}" else ""
                                        )
                                    }
                                    if (!violatesURLSpec)
                                        Text(
                                            text = it.name + ": ${it.value}",
                                            color = Color.Blue,
                                            modifier = Modifier.clickable {
                                                uriHandler.openUri(it.value!!)
                                            }
                                        )
                                } else
                                    Text(
                                        text = "${it.name}: ${it.value}",
                                        modifier = Modifier.onClick(onClick = { println("Clicked $it") })
                                    )
                            }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
