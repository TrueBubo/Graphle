package com.graphle.graphlemanager.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

private val mockStorage = object : Storage {
    override fun set(key: String, value: String) {}
    override fun get(key: String): String? = null
    override fun hset(key: String, hash: Map<String, String>) {}
    override fun hgetAll(key: String): Map<String, String> = mapOf()
    override fun sadd(key: String, value: String) {}
    override fun smembers(key: String): Set<String> = emptySet()
    override fun expire(key: String, seconds: Long) {}
}

class ValkeyFilenameCompleterTest() {
    @Test
    fun `filename completer completes last level filenames`() {
        val filenameCompleter = FilenameCompleter(mockStorage)
        filenameCompleter.insert(listOf("home", "user"))
        filenameCompleter.insert(listOf("home", "user", "notThere"))
        assertEquals(
            listOf(listOf("home", "user", "notThere")),
            filenameCompleter.lookup("no") { true }
        )
    }

    @Test
    fun `filename completer completes full level filenames`() {
        val filenameCompleter = FilenameCompleter(mockStorage)
        filenameCompleter.insert(listOf("home", "user"))
        filenameCompleter.insert(listOf("home", "user", "notThere"))
        assertEquals(
            listOf(listOf("home", "user", "notThere"), listOf("home", "user")).toSet(),
            filenameCompleter.lookup("/ho") { true }
                .toSet()
        )
    }

    @Test
    fun `filename completer only returns existing files`() {
        val filenameCompleter = FilenameCompleter(mockStorage)
        filenameCompleter.insert(listOf("home", "user"))
        filenameCompleter.insert(listOf("home", "user", "notThere"))
        assertEquals(
            listOf(listOf("home", "user", "notThere")),
            filenameCompleter.lookup("/ho")  { it.contains("notThere") }
        )
    }
}