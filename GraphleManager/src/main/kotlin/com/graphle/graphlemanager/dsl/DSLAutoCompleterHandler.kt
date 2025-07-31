package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * WebSocket handler for autocomplete
 * @param registry Registry to handle different sessions
 * @param dslAutoCompleter Autocomplete used for sending messages
 */
@Service
class DSLAutoCompleterHandler(private val registry: SessionRegistry, private val dslAutoCompleter: DSLAutoCompleter) : TextWebSocketHandler() {
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
     * Sends the messages in response to message from the client. Responds with possible continuations represented as json
     * @param session Origin of the message
     * @param messageReceived Message from client
     * @see DSLAutoCompleter.complete
     */
    override fun handleTextMessage(session: WebSocketSession, messageReceived: TextMessage) {
        val input = messageReceived.payload
        val messageSent = Json.encodeToString(dslAutoCompleter.complete(input))
        session.sendMessage(TextMessage(messageSent))
    }
}