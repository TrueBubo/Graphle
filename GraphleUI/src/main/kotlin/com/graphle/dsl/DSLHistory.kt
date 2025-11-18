package com.graphle.dsl

import com.graphle.common.model.DisplayedSettings
import com.graphle.common.userHome
import kotlinx.coroutines.flow.MutableStateFlow

object DSLHistory {
    val lastDisplayedCommand = MutableStateFlow("detail \"$userHome\"")

    suspend fun repeatLastDisplayedCommand(onResult: (DisplayedSettings) -> Unit) {
        handleDslCommand(
            dslCommand = lastDisplayedCommand.value,
            onEvaluation = onResult
        )
    }
}