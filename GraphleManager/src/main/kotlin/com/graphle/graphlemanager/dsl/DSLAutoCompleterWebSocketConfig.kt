package com.graphle.graphlemanager.dsl

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class DSLAutoCompleterWebSocketConfig(private val dslAutoCompleter: DSLAutoCompleter) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(DSLAutoCompleterHandler(SessionRegistry, dslAutoCompleter), "/ws")
            .setAllowedOriginPatterns("*")
    }
}