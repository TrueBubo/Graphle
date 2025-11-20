package com.graphle.dsl.util

import kotlinx.serialization.json.Json

/**
 * Safely decodes a JSON string to a typed object, returning null on error.
 *
 * @param value JSON string to decode
 * @return Decoded object or null if decoding fails
 */
inline fun <reified T> Json.decodeFromStringOrNull(value: String): T? = try {
    Json.decodeFromString<T>(value)
} catch (_: Exception) {
    null
}