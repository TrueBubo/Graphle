package com.graphle.graphlemanager.dsl

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Class to which properties related to autocomplete are parsed to
 */
@ConfigurationProperties(prefix = "autocomplete")
data class AutoCompleteProperties(val valkey: Valkey) {
    /**
     * Properties related to Valkey database configuration
     * @param host Host address
     * @param port Post on the host
     * @see ValkeyFilenameCompleterService
     */
    data class Valkey(val host: String, val port: Int)
}