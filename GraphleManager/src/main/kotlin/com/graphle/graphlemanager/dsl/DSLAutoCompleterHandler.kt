package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Service
class DSLAutoCompleterHandler(private val registry: SessionRegistry) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        registry.addSession(Session(session.id), session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.removeSession(Session(session.id))
    }
    override fun handleTextMessage(session: WebSocketSession, messageReceived: TextMessage) {
        val input = messageReceived.payload
        val messageSent = DSLAutoCompleter().complete(input)
            .joinToString(prefix = "[", postfix = "]", transform = { "\"$it\""})
        session.sendMessage(TextMessage(messageSent))
    }
}