package com.graphle.header.util

import com.graphle.common.dslURL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Type of response from DSL command execution.
 */
enum class ResponseType {
    ERROR,
    SUCCESS,
    FILENAMES,
    CONNECTIONS,
    FILE,
    TAG
}

/**
 * Request payload for DSL command execution.
 *
 * @property command The DSL command string
 */
@Serializable
data class DSLRequest(val command: String)

/**
 * Response payload from DSL command execution.
 *
 * @property type Type of response
 * @property responseObject List of response data strings
 */
@Serializable
data class DSLResponse(val type: ResponseType, val responseObject: List<String>)

/**
 * Manages REST communication for DSL command interpretation.
 */
object DSLRestManager {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    /**
     * Sends a DSL command to the server for interpretation.
     *
     * @param command The DSL command to execute
     * @return Server response with execution results
     */
    suspend fun interpretCommand(command: String): DSLResponse {
        try {
            return client.post(dslURL) {
                contentType(ContentType.Application.Json)
                setBody(DSLRequest(command))
            }.body()
        } catch (e: Exception) {
            throw e
        }
    }
}