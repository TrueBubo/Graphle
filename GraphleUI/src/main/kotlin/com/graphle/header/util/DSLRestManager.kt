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

@Serializable
data class DSLRequest(val command: String)

@Serializable
data class DSLResponse(val type: String, val responseObject: List<String>? = null)

object DSLRestManager {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    suspend fun interpretCommand(command: String): DSLResponse {
        try {
            return client.post(dslURL) {
                contentType(ContentType.Application.Json)
                setBody(DSLRequest(command))
            }.body()
        } catch (e: Exception) {
            println(command)
            throw e
        }
    }
}