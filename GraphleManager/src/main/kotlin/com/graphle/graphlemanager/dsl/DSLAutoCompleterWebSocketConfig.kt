package com.graphle.graphlemanager.dsl

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * Configuration for autocomplete websockets
 * @param dslAutoCompleter Autocomplete for handing requests
 */
@Configuration
@EnableWebSocket
open class DSLAutoCompleterWebSocketConfig(private val dslAutoCompleter: DSLAutoCompleter) : WebSocketConfigurer {
    /**
     * Registers WebSocket handlers for autocomplete functionality.
     * @param registry The WebSocket handler registry to configure
     */
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(DSLAutoCompleterHandler(SessionRegistry, dslAutoCompleter), "/ws")
            .setAllowedOriginPatterns("*")
    }
}