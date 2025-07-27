package com.graphle

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object DSLWebSocketManager {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private var session: DefaultClientWebSocketSession? = null
    private val _messages = MutableSharedFlow<List<String>>(replay = 0)
    val messages: SharedFlow<List<String>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val sendQueue = Channel<String>(capacity = Channel.UNLIMITED)

    fun connect() {
        if (_isConnected.value) return // avoid reconnecting
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = "localhost",
                    port = 8080,
                    path = "/ws"
                ) {
                    session = this
                    _isConnected.value = true
                    println("WebSocket connected")

                    // Launch sender
                    launch {
                        for (msg in sendQueue) {
                            println("Sending: $msg")
                            send(Frame.Text(msg))
                        }
                    }

                    // Receive incoming messages
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("Received: $text")
                            try {
                                val parsed = Json.decodeFromString<List<String>>(text)
                                _messages.emit(parsed)
                            } catch (e: Exception) {
                                println("Decode error: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.message}")
            } finally {
                _isConnected.value = false
                println("WebSocket closed")
            }
        }
    }

    suspend fun sendAutocompleteRequest(input: String) {
        sendQueue.send(input)
    }
}
