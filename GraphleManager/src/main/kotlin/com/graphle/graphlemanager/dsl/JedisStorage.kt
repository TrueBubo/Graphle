package com.graphle.graphlemanager.dsl

import io.valkey.Jedis

/**
 * Storage implementation for Valkey
 * @param jedis Handler for Valkey operations
 * @see Storage
 */
class JedisStorage(private val jedis: Jedis) : Storage {

    override fun set(key: String, value: String) {
        jedis.set(key, value)
    }

    override fun get(key: String): String? {
        return jedis.get(key)
    }

    override fun hset(key: String, hash: Map<String, String>) {
        jedis.hset(key, hash)
    }

    override fun hgetAll(key: String): Map<String, String> {
        return jedis.hgetAll(key)
    }

    override fun sadd(key: String, value: String) {
        jedis.sadd(key, value)
    }

    override fun smembers(key: String): Set<String> {
        return jedis.smembers(key)
    }

    override fun expire(key: String, seconds: Long) {
        jedis.expire(key, seconds)
    }


}