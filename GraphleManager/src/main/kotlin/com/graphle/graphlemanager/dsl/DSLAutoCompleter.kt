package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service
import java.io.File

@Service
class DSLAutoCompleter {
    private fun completeFilename(filenamePrefix: String): List<String> =
        ValkeyFilenameCompleter.lookup(filenamePrefix).map { it.joinToString(File.separator) }


    fun complete(commandPrefix: String): List<String> =
        listOf(commandPrefix, "$commandPrefix + the rest") + completeFilename(commandPrefix)
}