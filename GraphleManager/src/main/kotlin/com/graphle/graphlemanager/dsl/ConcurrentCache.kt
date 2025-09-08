package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.commons.CoroutineDelayer
import com.graphle.graphlemanager.commons.IDelayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Holds value which has a time at which it is expired
 * @param value Value held
 * @param expires Instant at which the value is no longer valid
 */
data class ExpirableValue<T>(val value: T, val expires: Instant) {
    /**
     * @param at Is the value valid at that moment
     * @return Whether the value is valid at that moment
     */
    fun isExpired(at: Instant = Instant.now()): Boolean = expires.isBefore(at)
}

/**
 * Thread safe cache implementation
 * @param K Type of key
 * @param V Type of value
 * @param ttl Values stored will be stored for this long from last access
 * @param sweepInterval Expired values will be periodically deleted this often
 * @param shouldTriggerCacheSweep Will only trigger sweep the given condition is true
 */
class ConcurrentCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val sweepInterval: Duration = 1.minutes,
    private val shouldTriggerCacheSweep: (Map<K, ExpirableValue<V>>).() -> Boolean = { true },
    private val onSweep: () -> Unit = {},
    private val delayer: IDelayer = CoroutineDelayer()
) {
    val size: Int get() = cache.size
    private val cache = ConcurrentHashMap<K, ExpirableValue<V>>()
    private val sweeperScope = CoroutineScope(Dispatchers.IO)

    init {
        sweeperScope.launch { sweep() }
    }

    /**
     * Periodically cleans the cache of expired values
     */
    private suspend fun sweep() {
        while (true) {
            delayer.delay(sweepInterval)
            if (!cache.shouldTriggerCacheSweep()) continue
            val now = Instant.now()
            cache.asSequence()
                .filter { it.value.isExpired(now) }
                .forEach { println("Removed"); cache.remove(it.key) }
            onSweep()
        }
    }

    /**
     * Sets the value to a given key
     */
    operator fun set(key: K, value: V) =
        cache.put(key, ExpirableValue(value, Instant.now() + ttl.toJavaDuration()))?.value

    /**
     * Gets the value at key
     * @param key Key in the cache
     * @return value if present and valid, else null
     */
    operator fun get(key: K): V? {
        val value = cache[key]
        if (value == null || value.isExpired()) return null
        set(key, value.value)
        return value.value
    }
}