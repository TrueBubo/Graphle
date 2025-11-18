package com.graphle.graphlemanager.dsl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import java.time.Duration

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
        val sweepInterval = 10.minutes
        val cache = ConcurrentCache<String, String>(
            ttl = 0.minutes,
            sweepInterval = sweepInterval,
            onSweep = { swept = true },
            delayer = delayer,
        )
        cache["hello"] = "world"
        assertEquals(1, cache.size)
        delayer.forwardTime(sweepInterval)
        val start = Instant.now()
        val sweepTimeLimitMillis = 100
        while (!swept) {
            if (Duration.between(start, Instant.now()).toMillisPart() > sweepTimeLimitMillis) {
                fail("The sweeper takes too long to sweep")
            }
        }
        assertEquals(0, cache.size)
    }

    @Test
    fun `overwriting existing key updates value`() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        cache["hello"] = "world"
        cache["hello"] = "universe"
        assertEquals("universe", cache["hello"])
        assertEquals(1, cache.size)
    }

    @Test
    fun `set returns previous value when overwriting`() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        assertNull(cache.set("hello", "world"))
        assertEquals("world", cache.set("hello", "universe"))
    }

    @Test
    fun `accessing value refreshes TTL`() = runBlocking {
        val delayer = MockDelayer()
        val ttl = 5.minutes
        val sweepInterval = 10.minutes
        val cache = ConcurrentCache<String, String>(
            ttl = ttl,
            sweepInterval = sweepInterval,
            delayer = delayer,
        )
        cache["hello"] = "world"

        delayer.forwardTime(ttl - 1.minutes)
        assertEquals("world", cache["hello"])

        delayer.forwardTime(2.minutes)
        assertEquals("world", cache["hello"])
    }

    @Test
    fun `multiple keys can be stored and retrieved independently`() {
        val cache = ConcurrentCache<String, Int>(10.minutes)
        cache["one"] = 1
        cache["two"] = 2
        cache["three"] = 3

        assertEquals(3, cache.size)
        assertEquals(1, cache["one"])
        assertEquals(2, cache["two"])
        assertEquals(3, cache["three"])
    }

    @Test
    fun `accessing expired value returns null`() {
        val cache = ConcurrentCache<String, String>((-10).minutes)
        cache["hello"] = "world"
        assertNull(cache["hello"])
    }

    @Test
    fun `empty cache has size zero`() {
        val cache = ConcurrentCache<String, String>(10.minutes)
        assertEquals(0, cache.size)
    }
}