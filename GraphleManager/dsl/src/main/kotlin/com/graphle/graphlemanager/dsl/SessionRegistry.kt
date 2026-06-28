package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds information about the sessions
 */
@Component
object SessionRegistry {
    private val sessions = ConcurrentHashMap<Session, WebSocketSession>()

    /**
     * Adds a session
     * @param id Identifier of the session
     * @param session session itself
     */
    fun addSession(id: Session, session: WebSocketSession) {
        sessions[id] = session
    }

    /**
     * Removes a session
     * @param id Identifier of the session
     */
    fun removeSession(id: Session) = sessions.remove(id)

    /**
     * Gets the session with given id
     * @param id Session to find
     */
    fun getSession(id: Session) = sessions[id]

    /**
     * Sends the message to every session
     * @param message Message to send
     */
    fun broadcast(message: String) {
        sessions.values.filter { it.isOpen }.forEach {
                it.sendMessage(TextMessage(message))
        }
    }
}