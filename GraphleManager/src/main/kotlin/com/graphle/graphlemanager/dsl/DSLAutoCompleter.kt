package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service

@Service
class DSLAutoCompleter {
    private fun completeFilename(filenamePrefix: String): List<String> {
        TODO()
    }

    fun complete(commandPrefix: String): List<String> =
        listOf(commandPrefix, "$commandPrefix + the rest")
}