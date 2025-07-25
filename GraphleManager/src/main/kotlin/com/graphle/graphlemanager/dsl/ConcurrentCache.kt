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

class ConcurrentCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val sweepInterval: Duration = 1.minutes,
    private val shouldTriggerCacheSweep: (Map<K, Pair<V, Instant>>).() -> Boolean = { true },
) {
    val size: Int get() = cache.size
    private val cache = ConcurrentHashMap<K, Pair<V, Instant>>()
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
                .filter { it.value.second <= now }
                .forEach { cache.remove(it.key) }
        }
    }

    operator fun set(key: K, value: V) = cache.put(key, value to Instant.now() + ttl.toJavaDuration())?.first

    operator fun get(key: K): V? {
        val value = cache.get(key)
        value?.let { set(key, value.first) }
        return value?.first
    }
}