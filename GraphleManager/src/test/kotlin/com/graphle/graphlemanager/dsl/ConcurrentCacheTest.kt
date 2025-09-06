package com.graphle.graphlemanager.dsl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

class ConcurrentCacheTest {
    @Test
    fun set() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        cache["hello"] = "world"
        cache["world"] = "world"
        assertEquals(2, cache.size)
    }

    @Test
    fun `gets value in cache`() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        cache["hello"] = "world"
        assertEquals("world", cache["hello"])
    }

    @Test
    fun `gets value not in cache`() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        cache["hello"] = "world"
        assertNull(cache[""])
    }

    @Test
    fun `values expire in cache`() {
        val cache = ConcurrentCache<String, String>((-10).minutes)
        cache["hello"] = "world"
        assertNull(cache["hello"])
    }

    @Test
    fun `values expire only after duration`() = runBlocking {
        val delayer = MockDelayer()
        var swept = false
        val cache = ConcurrentCache<String, String>(
            ttl = 0.minutes,
            sweepInterval = 10.minutes,
            onSweep = { swept = true },
            delayer = delayer,
        )
        cache["hello"] = "world"
        assertEquals(1, cache.size)
        delayer.forwardTime(100.minutes)
        val start = Instant.now()
        while (!swept) {
            if (java.time.Duration.between(start, Instant.now()).toMillisPart() > 100) {
                fail("The sweeper takes too long to sweep")
            }
        }
        assertEquals(0, cache.size)
    }
}