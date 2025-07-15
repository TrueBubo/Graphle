package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
object SessionRegistry {
    private val sessions = ConcurrentHashMap<Session, WebSocketSession>()

    fun addSession(id: Session, session: WebSocketSession) {
        sessions[id] = session
    }

    fun removeSession(id: Session) = sessions.remove(id)

    fun getSession(id: Session) = sessions[id]

    fun broadcast(message: String) {
        sessions.values.filter { it.isOpen }.forEach {
                it.sendMessage(TextMessage(message))
        }
    }
}