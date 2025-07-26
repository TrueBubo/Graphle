package com.graphle.graphlemanager.dsl

import kotlin.time.Duration

interface Storage {
    fun set(key: String, value: String)
    fun get(key: String): String?
    fun hset(key: String, hash: Map<String, String>)
    fun hgetAll(key: String): Map<String, String>
    fun sadd(key: String, value: String)
    fun smembers(key: String): Set<String>
    fun expire(key: String, seconds: Long)

    fun getEx(key: String, ttl: Duration, onGet: (String) -> String?): String? {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: get(key)
    }

    fun setEx(key: String, ttl: Duration, value: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        set(key, value)
        onSet(key, value)
    }

    fun hsetex(
        key: String,
        ttl: Duration,
        hash: Map<String, String>,
        onSet: (String, Map<String, String>) -> Unit
    ) {
        expire(key, ttl.inWholeSeconds)
        hset(key, hash)
        onSet(key, hash)
    }

    fun hgetAllEx(
        key: String,
        ttl: Duration,
        onGet: (String) -> Map<String, String>?
    ): Map<String, String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: hgetAll(key)
    }

    fun saddex(key: String, ttl: Duration, member: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        sadd(key, member)
        onSet(key, member)
    }

    fun smembersex(key: String, ttl: Duration, onGet: (String) -> Set<String>?): Set<String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: smembers(key)
    }
}

