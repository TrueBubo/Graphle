package com.graphle.dsl

import com.graphle.common.model.DisplayMode
import com.graphle.common.model.DisplayedData
import com.graphle.common.model.DisplayedSettings
import com.graphle.dialogs.ErrorMessage
import com.graphle.dsl.util.decodeFromStringOrNull
import com.graphle.file.model.Connection
import com.graphle.file.model.File
import com.graphle.header.util.DSLResponse
import com.graphle.header.util.DSLRestManager
import com.graphle.header.util.ResponseType.*
import kotlinx.serialization.json.Json
import java.lang.System.lineSeparator

suspend fun handleDslCommand(
    dslCommand: String,
    onEvaluation: (DisplayedData?) -> Unit,
    onModeUpdated: (DisplayMode) -> Unit,
) {
    val displayedSettings = parseDslResponse(
        response = DSLRestManager.interpretCommand(dslCommand)
    )
    if (displayedSettings == null) return
    onEvaluation(displayedSettings.displayedData)
    onModeUpdated(displayedSettings.displayedModel)
}


private fun parseDslResponse(
    response: DSLResponse
): DisplayedSettings? = when (response.type) {
    ERROR -> {
        ErrorMessage.set(
            showErrorMessage = true,
            errorMessage = response.responseObject.joinToString(lineSeparator())
        )
        null
    }

    SUCCESS -> null
    FILENAMES -> DisplayedSettings(
        displayedData = DisplayedData(
            filenames = response.responseObject,
        ),
        displayedModel = DisplayMode.Filenames
    )

    CONNECTIONS -> {
        val responses = response.responseObject.map {
            Json.decodeFromStringOrNull<Connection>(it)
        }
        if (responses.any { it == null }) {
            ErrorMessage.set(
                showErrorMessage = true,
                errorMessage = "Could not parse the response from the server"
            )
            null
        } else DisplayedSettings(
            displayedData = DisplayedData(connections = responses.filterNotNull()),
            displayedModel = DisplayMode.Connections
        )
    }

    FILE -> response.responseObject.firstOrNull()?.let {
        Json.decodeFromStringOrNull<File>(it)
    }?.let {
        DisplayedSettings(
            displayedData = DisplayedData(file = it),
            displayedModel = DisplayMode.File
        )
    }
}
