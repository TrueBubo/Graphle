package com.graphle.graphlemanager.dsl

import org.springframework.stereotype.Service
import java.io.File

/**
 * Used as a response getter for a prefix entered from GUI
 */
@Service
class DSLAutoCompleter(filenameCompleterService: ValkeyFilenameCompleter) {
    val filenameCompleter = filenameCompleterService.filenameCompleter
    /**
     * Finds out the files beginning with the given prefix
     * @param filenamePrefix Looking for files with this prefix
     * @param limit return at most this many entries
     * @return At most [limit] files matching the given prefix
     */
    private fun completeFilename(filenamePrefix: String, limit: Int): List<String> =
        filenameCompleter.lookup(filenamePrefix, limit).map { it.joinToString(File.separator) }

    /**
     * Predicts the DSL command with the next word
     */
    fun complete(commandPrefix: String, limit: Int = 5): List<String> {
        val completionList = completeFilename(commandPrefix, limit)
        println("$commandPrefix $completionList")
        return completionList
    }
}