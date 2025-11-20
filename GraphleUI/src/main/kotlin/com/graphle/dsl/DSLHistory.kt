package com.graphle.dsl

import com.graphle.common.model.DisplayedSettings
import com.graphle.common.userHome
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Manages DSL command history for the application.
 */
object DSLHistory {
    /**
     * The most recently executed display command.
     */
    val lastDisplayedCommand = MutableStateFlow("detail \"$userHome\"")

    /**
     * Re-executes the last displayed command.
     *
     * @param onResult Callback with the command result
     */
    suspend fun repeatLastDisplayedCommand(onResult: (DisplayedSettings) -> Unit) {
        handleDslCommand(
            dslCommand = lastDisplayedCommand.value,
            onEvaluation = onResult
        )
    }
}