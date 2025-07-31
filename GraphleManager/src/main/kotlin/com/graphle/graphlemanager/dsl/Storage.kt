package com.graphle.graphlemanager.dsl

import kotlin.time.Duration

/**
 * Interface for a key value storage
 */
interface Storage {
    /**
     * Sets a string value for the given key.
     *
     * @param key The key under which the value will be stored.
     * @param value The string value to store.
     */
    fun set(key: String, value: String)

    /**
     * Retrieves the string value associated with the given key.
     *
     * @param key The key to look up.
     * @return The string value if the key exists, or null otherwise.
     */
    fun get(key: String): String?

    /**
     * Sets multiple field-value pairs in a hash stored at the given key.
     *
     * @param key The key of the hash.
     * @param hash A map of field-value pairs to set in the hash.
     */
    fun hset(key: String, hash: Map<String, String>)

    /**
     * Retrieves all field-value pairs from the hash stored at the given key.
     *
     * @param key The key of the hash.
     * @return A map containing all field-value pairs in the hash.
     */
    fun hgetAll(key: String): Map<String, String>

    /**
     * Adds a value to a set stored at the given key.
     *
     * @param key The key of the set.
     * @param value The value to add to the set.
     */
    fun sadd(key: String, value: String)

    /**
     * Retrieves all members of the set stored at the given key.
     *
     * @param key The key of the set.
     * @return A set containing all members of the stored set.
     */
    fun smembers(key: String): Set<String>

    /**
     * Sets a time-to-live (TTL) in seconds for the given key.
     * After the specified time, the key will expire and be automatically deleted.
     *
     * @param key The key to set expiration on.
     * @param seconds The TTL in seconds.
     */
    fun expire(key: String, seconds: Long)

    /**
     * Gets the value, and set its expiry
     * @param key The key to look up
     * @param ttl Expires in this long
     * @param onGet Tries to find the given key using this first
     * @return The string value if the key exists, or null otherwise.
     */
    fun getEx(key: String, ttl: Duration, onGet: (String) -> String?): String? {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: get(key)
    }

    /**
     * Sets the value, and its expiry
     * @param key The key to look up
     * @param ttl Expires in this long
     * @param value The value to insert
     * @param onSet Does this operation after setting the key
     * @return The string value if the key exists, or null otherwise.
     */
    fun setEx(key: String, ttl: Duration, value: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        set(key, value)
        onSet(key, value)
    }

    /**
     * Sets the map, and its expiry
     * @param key The key to set
     * @param ttl Expires in this long
     * @param hash The value to set
     * @param onSet Does this operation after setting the key
     */
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

    /**
     * Gets the map, and sets its expiry
     * @param key The key to look up
     * @param ttl Expires in this long
     * @param onGet Tries to find the given key using this first first
     * @return The map
     */
    fun hgetAllEx(
        key: String,
        ttl: Duration,
        onGet: (String) -> Map<String, String>?
    ): Map<String, String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: hgetAll(key)
    }

    /**
     * Adds the member to set, and updates sets expiry
     * @param key The key to set
     * @param ttl Expires in this long
     * @param member The value to add
     * @param onSet Does this operation after setting the key
     */
    fun saddex(key: String, ttl: Duration, member: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        sadd(key, member)
        onSet(key, member)
    }

    /**
     * Gets all the members of the set, and updates sets expire
     * @param key The set to find members of
     * @param ttl Expires in this long
     * @param onGet Tries to find the given key using this first
     * @return Members of the set
     */
    fun smembersex(key: String, ttl: Duration, onGet: (String) -> Set<String>?): Set<String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: smembers(key)
    }
}

