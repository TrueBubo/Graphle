package com.graphle.header.util

import com.graphle.common.config
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
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
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal fun decodeAutocompleteResponse(text: String): List<String> {
    if (text.isEmpty()) return emptyList()

    val values = ArrayList<String>(5)
    var index = 0
    while (index < text.length) {
        val lengthStart = index
        var length = 0
        while (index < text.length && text[index].isDigit()) {
            length = length * 10 + (text[index] - '0')
            index++
        }

        if (index == lengthStart || index >= text.length || text[index] != ':') {
            throw IllegalArgumentException("Invalid autocomplete response")
        }

        index++
        val valueEnd = index + length
        if (valueEnd > text.length) {
            throw IllegalArgumentException("Incomplete autocomplete response")
        }

        values.add(text.substring(index, valueEnd))
        index = valueEnd
    }

    return values
}

private data class DecodedAutocompleteResponse(
    val requestId: Long?,
    val suggestions: List<String>,
)

private fun decodeAutocompleteResponseFrame(text: String): DecodedAutocompleteResponse {
    val separatorIndex = text.indexOf('\t')
    if (separatorIndex <= 0) {
        return DecodedAutocompleteResponse(
            requestId = null,
            suggestions = decodeAutocompleteResponse(text),
        )
    }

    val possibleId = text.substring(0, separatorIndex)
    if (!possibleId.all(Char::isDigit)) {
        return DecodedAutocompleteResponse(
            requestId = null,
            suggestions = decodeAutocompleteResponse(text),
        )
    }
    val requestId = possibleId.toLongOrNull() ?: return DecodedAutocompleteResponse(
        requestId = null,
        suggestions = decodeAutocompleteResponse(text),
    )

    return DecodedAutocompleteResponse(
        requestId = requestId,
        suggestions = decodeAutocompleteResponse(text.substring(separatorIndex + 1)),
    )
}

/**
 * Manages WebSocket connection for DSL autocomplete functionality.
 */
object DSLWebSocketManager {
    private val autocompleteDebounceDelay = 60.milliseconds
    private val autocompleteResponseTimeout = 5.seconds

    data class AutocompleteMessage(
        val input: String,
        val suggestions: List<String>,
    )

    private data class PendingAutocompleteRequest(
        val input: String,
    )

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val _messages = MutableSharedFlow<AutocompleteMessage>(replay = 0)

    /**
     * Flow of autocomplete suggestion messages from the server.
     */
    val messages: SharedFlow<AutocompleteMessage> = _messages

    private val _isConnected = MutableStateFlow(false)

    /**
     * Flow indicating current connection status.
     */
    val isConnected: StateFlow<Boolean> = _isConnected

    private val latestAutocompleteRequest = AtomicReference<String?>(null)
    private val requestSequence = AtomicLong(0)
    private val inFlightRequestId = AtomicReference<Long?>(null)
    private val requestIdsEnabled = AtomicBoolean(true)
    private val autocompleteRequestSignal = Channel<Unit>(capacity = Channel.CONFLATED)
    private val autocompleteResponseSignal = Channel<Long>(capacity = Channel.UNLIMITED)
    private val pendingRequests = ConcurrentHashMap<Long, PendingAutocompleteRequest>()

    private var connectionRetries = 0
    private var maxRetries = 2
    private val retryDelay = { retryIdx: Int -> 1000.milliseconds * 2.0.pow(retryIdx.toDouble()) }

    private val _isFailed = MutableStateFlow(false)

    /**
     * Flow indicating whether connection has permanently failed.
     */
    val isFailed: StateFlow<Boolean> = _isFailed

    private fun <T> Channel<T>.drain() {
        while (tryReceive().isSuccess) {
        }
    }

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
                try {
                    val decoded = decodeAutocompleteResponseFrame(text)
                    val pendingRequest = resolvePendingRequest(decoded)

                    if (decoded.requestId != null && pendingRequest == null) {
                        continue
                    }

                    _messages.emit(
                        AutocompleteMessage(
                            input = pendingRequest?.input.orEmpty(),
                            suggestions = decoded.suggestions,
                        )
                    )
                } catch (e: Exception) {
                    println("Receiving message parsing failed: ${e.message}")
                    inFlightRequestId.getAndSet(null)?.let {
                        pendingRequests.remove(it)
                        autocompleteResponseSignal.trySend(it)
                    }
                }
            }
        }
    }

    private fun resolvePendingRequest(decoded: DecodedAutocompleteResponse): PendingAutocompleteRequest? {
        decoded.requestId?.let { requestId ->
            inFlightRequestId.compareAndSet(requestId, null)
            autocompleteResponseSignal.trySend(requestId)
            return pendingRequests.remove(requestId)
        }

        val requestId = inFlightRequestId.getAndSet(null) ?: return null
        val pendingRequest = pendingRequests.remove(requestId) ?: return null
        autocompleteResponseSignal.trySend(requestId)

        if (requestIdsEnabled.getAndSet(false)) {
            latestAutocompleteRequest.set(pendingRequest.input)
            autocompleteRequestSignal.trySend(Unit)
        }

        return pendingRequest
    }

    private suspend fun DefaultClientWebSocketSession.sendQueuedMessages() {
        autocompleteRequestSignal.drain()
        autocompleteResponseSignal.drain()

        while (true) {
            autocompleteRequestSignal.receive()
            delay(autocompleteDebounceDelay)
            var msg = latestAutocompleteRequest.getAndSet(null) ?: continue

            while (true) {
                val requestId = sendAutocompleteRequestNow(msg)
                val responseReceived = withTimeoutOrNull(autocompleteResponseTimeout) {
                    waitForAutocompleteResponse(requestId)
                } != null

                if (!responseReceived) {
                    pendingRequests.remove(requestId)
                    inFlightRequestId.compareAndSet(requestId, null)
                }

                delay(autocompleteDebounceDelay)
                msg = latestAutocompleteRequest.getAndSet(null) ?: break
            }
        }
    }

    private suspend fun waitForAutocompleteResponse(requestId: Long) {
        while (autocompleteResponseSignal.receive() != requestId) {
        }
    }

    private suspend fun DefaultClientWebSocketSession.sendAutocompleteRequestNow(msg: String): Long {
        val requestId = requestSequence.incrementAndGet()
        pendingRequests[requestId] =
            PendingAutocompleteRequest(
                input = msg,
            )
        inFlightRequestId.set(requestId)
        try {
            val frameText = if (requestIdsEnabled.get()) "$requestId\t$msg" else msg
            send(Frame.Text(frameText))
        } catch (exception: Exception) {
            pendingRequests.remove(requestId)
            inFlightRequestId.compareAndSet(requestId, null)
            throw exception
        }
        return requestId
    }

    private fun markConnectionAsEstablished() {
        _isConnected.value = true
        requestIdsEnabled.set(true)
        connectionRetries = 0
        maxRetries = 10
        println("DSL WebSocket connection established")

    }

    /**
     * Establishes WebSocket connection to the server.
     */
    fun connect() {
        if (_isConnected.value) return // avoid reconnecting
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(
                    method = HttpMethod.Companion.Get,
                    host = "localhost",
                    port = config.server.port,
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
                latestAutocompleteRequest.set(null)
                pendingRequests.clear()
                inFlightRequestId.set(null)
                autocompleteResponseSignal.drain()
                tryToReconnect("DSL WebSocket closed")
            }
        }
    }

    /**
     * Sends an autocomplete request for the given input.
     *
     * @param input Command text to get autocomplete suggestions for
     */
    fun sendAutocompleteRequest(input: String) {
        latestAutocompleteRequest.set(input)
        autocompleteRequestSignal.trySend(Unit)
    }
}
