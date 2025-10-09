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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

object DSLWebSocketManager {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val _messages = MutableSharedFlow<List<String>>(replay = 0)
    val messages: SharedFlow<List<String>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val sendQueue = Channel<String>(capacity = Channel.UNLIMITED)

    private var connectionRetries = 0
    private var maxRetries = 2
    private val retryDelay = { retryIdx: Int -> 1000.milliseconds * 2.0.pow(retryIdx.toDouble()) }

    private val _isFailed = MutableStateFlow(false)
    val isFailed: StateFlow<Boolean> = _isFailed

    private suspend inline fun tryToReconnect(failMessage: String) {
        if (connectionRetries >= maxRetries) {
            println(failMessage)
            _isFailed.value = true
            return
        }
        delay(retryDelay(connectionRetries))
        connectionRetries++
        connect()
    }

    private suspend fun DefaultClientWebSocketSession.receiveIncomingMessages() {
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                println(text)
                try {
                    val parsed = Json.decodeFromString<List<String>>(text)
                    _messages.emit(parsed)
                } catch (e: Exception) {
                    println("Receiving message parsing failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.sendQueuedMessages() {
        for (msg in this@DSLWebSocketManager.sendQueue) {
            send(Frame.Text(msg))
        }
    }

    private fun markConnectionAsEstablished() {
        _isConnected.value = true
        connectionRetries = 0
        maxRetries = 10
        println("DSL WebSocket connection established")

    }

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
                    markConnectionAsEstablished()
                    launch { sendQueuedMessages() }
                    receiveIncomingMessages()
                }
            } catch (e: Exception) {
                tryToReconnect("DSL WebSocket error: ${e.message}")
            } finally {
                _isConnected.value = false
                tryToReconnect("DSL WebSocket closed")
            }
        }
    }

    suspend fun sendAutocompleteRequest(input: String) {
        sendQueue.send(input)
    }
}
