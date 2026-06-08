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

private class SessionCountingStorage(private val delegate: Storage) : Storage by delegate {
    var sessionCount = 0

    override fun <T> withSession(action: (Storage) -> T): T {
        sessionCount++
        return action(this)
    }
}

private class FailOnRouteReconstructionStorage(private val delegate: Storage) : Storage by delegate {
    override fun <T> withSession(action: (Storage) -> T): T {
        return action(this)
    }

    override fun getEx(key: String, ttl: kotlin.time.Duration, onGet: (String) -> String?): String? {
        check(!key.endsWith(":prev") && !key.endsWith(":val")) {
            "route reconstruction should not be used for this lookup"
        }
        return delegate.getEx(key, ttl, onGet)
    }

    override fun get(key: String): String? {
        check(!key.endsWith(":prev") && !key.endsWith(":val")) {
            "route reconstruction should not be used for this lookup"
        }
        return delegate.get(key)
    }
}

private class FullPathMarkerCheckingStorage(private val delegate: Storage) : Storage by delegate {
    val fullPathMarkers = mutableListOf<String>()

    override fun <T> withSession(action: (Storage) -> T): T {
        return action(this)
    }

    override fun setEx(key: String, ttl: kotlin.time.Duration, value: String, onSet: (String, String) -> Unit) {
        if (key.endsWith(":full")) fullPathMarkers.add(value)
        delegate.setEx(key, ttl, value, onSet)
    }

    override fun set(key: String, value: String) {
        if (key.endsWith(":full")) fullPathMarkers.add(value)
        delegate.set(key, value)
    }
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

    @Test
    fun `filename completer opens one storage session per lookup`() {
        val storage = SessionCountingStorage(mockStorage)
        val filenameCompleter = FilenameCompleter(storage)
        filenameCompleter.insert(listOf("home", "user", "notThere"))

        storage.sessionCount = 0
        filenameCompleter.lookup("no") { true }

        assertEquals(1, storage.sessionCount)
    }

    @Test
    fun `filename completer stops existence checks after limit is reached`() {
        val filenameCompleter = FilenameCompleter(mockStorage)
        filenameCompleter.insert(listOf("home", "user", "first"))
        filenameCompleter.insert(listOf("other", "user", "first"))

        var existenceChecks = 0
        val results = filenameCompleter.lookup("first", limit = 1) {
            existenceChecks++
            true
        }

        assertEquals(1, results.size)
        assertEquals(1, existenceChecks)
    }

    @Test
    fun `filename completer uses stored full path for bottom level matches`() {
        val storage = FullPathMarkerCheckingStorage(FailOnRouteReconstructionStorage(mockStorage))
        val filenameCompleter = FilenameCompleter(storage)
        filenameCompleter.insert(listOf("home", "user", "notThere"))

        assertEquals(listOf("1"), storage.fullPathMarkers)
        assertEquals(
            listOf(listOf("home", "user", "notThere")),
            filenameCompleter.lookup("not") { true }
        )
    }
}
