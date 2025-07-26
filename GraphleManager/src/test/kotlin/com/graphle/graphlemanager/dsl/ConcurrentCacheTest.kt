package com.graphle.graphlemanager.dsl

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

}