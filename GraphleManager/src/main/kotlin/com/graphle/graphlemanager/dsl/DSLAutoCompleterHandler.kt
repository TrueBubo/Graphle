package com.graphle.graphlemanager.dsl

import io.valkey.exceptions.JedisException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

internal fun encodeAutocompleteResponse(values: List<String>): String {
    if (values.isEmpty()) return ""
    val capacity = values.sumOf { it.length + it.length.toString().length + 1 }
    return buildString(capacity) {
        values.forEach { value ->
            append(value.length)
            append(':')
            append(value)
        }
    }
}

/**
 * WebSocket handler for autocomplete
 * @param registry Registry to handle different sessions
 * @param dslAutoCompleter Autocomplete used for sending messages
 */
@Service
class DSLAutoCompleterHandler(private val registry: SessionRegistry, private val dslAutoCompleter: DSLAutoCompleter) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(DSLAutoCompleterHandler::class.java)

    /**
     * adds the session to registry when connected
     * @param session Session added
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        registry.addSession(Session(session.id), session)
    }

    /**
     * removes the session from registry when disconnected
     * @param session Session disconnected
     * @param status Why and how was the session disconnected
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.removeSession(Session(session.id))
    }

    /**
     * Sends the messages in response to message from the client.
     * @param session Origin of the message
     * @param messageReceived Message from client
     * @see DSLAutoCompleter.complete
     */
    override fun handleTextMessage(session: WebSocketSession, messageReceived: TextMessage) {
        val input = messageReceived.payload
        val completions = try {
            dslAutoCompleter.complete(input)
        } catch (exception: JedisException) {
            logger.warn("Autocomplete Valkey lookup failed", exception)
            emptyList()
        } catch (exception: RuntimeException) {
            logger.warn("Autocomplete lookup failed", exception)
            emptyList()
        }
        val messageSent = encodeAutocompleteResponse(completions)
        session.sendMessage(TextMessage(messageSent))
    }
}
