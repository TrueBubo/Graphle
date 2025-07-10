package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service

@Service
class DSLAutoCompleter {
    fun complete(commandPrefix: String): List<String> =
        listOf(commandPrefix, "$commandPrefix + the rest")
}