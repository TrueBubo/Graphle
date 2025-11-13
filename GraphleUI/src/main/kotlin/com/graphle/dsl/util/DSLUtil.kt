package com.graphle.dsl.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


inline fun <reified T> Json.decodeFromStringOrNull(value: String): T? = try {
    Json.decodeFromString<T>(value)
} catch (_: Exception) {
    null
}