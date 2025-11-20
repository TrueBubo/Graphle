package com.graphle.graphlemanager.dsl

/**
 * Service interface for providing filename completion functionality.
 */
interface FilenameCompleterService {
    /**
     * The filename completer instance used for file path completion.
     */
    val completer: FilenameCompleter
}