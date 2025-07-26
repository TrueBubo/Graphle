package com.graphle.graphlemanager.dsl

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "autocomplete")
data class AutoCompleteProperties(val valkey: Valkey) {
    data class Valkey(val host: String, val port: Int)
}