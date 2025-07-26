package com.graphle.graphlemanager.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

data class ExpirableValue<T>(val value: T, val expires: Instant) {
    fun isExpired(now: Instant = Instant.now()): Boolean = expires.isBefore(now)
}

class ConcurrentCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val sweepInterval: Duration = 1.minutes,
    private val shouldTriggerCacheSweep: (Map<K, ExpirableValue<V>>).() -> Boolean = { true },
) {
    val size: Int get() = cache.size
    private val cache = ConcurrentHashMap<K, ExpirableValue<V>>()
    private val sweeperScope = CoroutineScope(Dispatchers.IO)

    init {
        sweeperScope.launch { sweep() }
    }

    private suspend fun sweep() {
        while (true) {
            delay(sweepInterval)
            if (!cache.shouldTriggerCacheSweep()) continue
            val now = Instant.now()
            cache.asSequence()
                .filter { it.value.isExpired(now) }
                .forEach { cache.remove(it.key) }
        }
    }

    operator fun set(key: K, value: V) =
        cache.put(key, ExpirableValue(value, Instant.now() + ttl.toJavaDuration()))?.value

    operator fun get(key: K): V? {
        val value = cache[key]
        if (value == null || value.isExpired()) return null
        set(key, value.value)
        return value.value
    }
}