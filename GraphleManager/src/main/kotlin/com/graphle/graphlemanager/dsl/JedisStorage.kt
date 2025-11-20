package com.graphle.graphlemanager.dsl

import io.valkey.Jedis

/**
 * Storage implementation for Valkey
 * @param jedis Handler for Valkey operations
 * @see Storage
 */
class JedisStorage(private val jedis: Jedis) : Storage {

    /**
     * Sets a string value for the given key.
     * @param key The key under which the value will be stored
     * @param value The string value to store
     */
    override fun set(key: String, value: String) {
        jedis.set(key, value)
    }

    /**
     * Retrieves the string value associated with the given key.
     * @param key The key to look up
     * @return The string value if the key exists, or null otherwise
     */
    override fun get(key: String): String? {
        return jedis.get(key)
    }

    /**
     * Sets multiple field-value pairs in a hash stored at the given key.
     * @param key The key of the hash
     * @param hash A map of field-value pairs to set in the hash
     */
    override fun hset(key: String, hash: Map<String, String>) {
        jedis.hset(key, hash)
    }

    /**
     * Retrieves all field-value pairs from the hash stored at the given key.
     * @param key The key of the hash
     * @return A map containing all field-value pairs in the hash
     */
    override fun hgetAll(key: String): Map<String, String> {
        return jedis.hgetAll(key)
    }

    /**
     * Adds a value to a set stored at the given key.
     * @param key The key of the set
     * @param value The value to add to the set
     */
    override fun sadd(key: String, value: String) {
        jedis.sadd(key, value)
    }

    /**
     * Retrieves all members of the set stored at the given key.
     * @param key The key of the set
     * @return A set containing all members of the stored set
     */
    override fun smembers(key: String): Set<String> {
        return jedis.smembers(key)
    }

    /**
     * Sets a time-to-live (TTL) in seconds for the given key.
     * @param key The key to set expiration on
     * @param seconds The TTL in seconds
     */
    override fun expire(key: String, seconds: Long) {
        jedis.expire(key, seconds)
    }


}