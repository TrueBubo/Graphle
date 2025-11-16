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
    onEvaluation: (DisplayedSettings) -> Unit,
) {
    System.err.println("[dslCommand] $dslCommand")
    val response = DSLRestManager.interpretCommand(dslCommand)
    val displayedSettings = parseDslResponse(
        response = response
    )

    if (displayedSettings == null) {
        DSLHistory.repeatLastDisplayedCommand(onEvaluation)
        return
    }
    DSLHistory.lastDisplayedCommand.value = dslCommand
    onEvaluation(displayedSettings)
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
        data = DisplayedData(
            filenames = response.responseObject,
        ),
        mode = DisplayMode.Filenames
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
            data = DisplayedData(connections = responses.filterNotNull()),
            mode = DisplayMode.Connections
        )
    }

    FILE -> response.responseObject.firstOrNull()?.let {
        Json.decodeFromStringOrNull<File>(it)
    }?.let {
        DisplayedSettings(
            data = DisplayedData(
                location = it.location,
                tags = it.tags,
                connections = it.connections,
            ),
            mode = DisplayMode.File
        )
    }
}
