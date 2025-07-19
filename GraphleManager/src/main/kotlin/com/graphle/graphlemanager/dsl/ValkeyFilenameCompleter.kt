package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import java.io.File
import kotlin.text.forEach
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@JvmInline
value class CharacterKey(val key: String)

inline fun <T> JedisPool.withJedis(action: (Jedis) -> T): T {
    return this.resource.use { jedis -> action(jedis) }
}

object ValkeyFilenameCompleter : FilenameCompleter {
    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 10
        maxIdle = 5
        minIdle = 1
        testOnBorrow = true
    }
    private const val HOST = "localhost"
    private const val PORT = 6379

    private val pool = JedisPool(poolConfig, HOST, PORT)
    private val ttl = 30.days
    private const val LAST_KEY = "last"
    private var lastElement = pool.withJedis { it.get(LAST_KEY) }?.toLong() ?: 0
    private val ROOT = CharacterKey("0")

    private fun Jedis.hsetex(key: String, ttl: Duration, hash: Map<String, String>) {
        hset(key, hash)
        expire(key, ttl.inWholeSeconds)
    }

    private fun Jedis.hgetAllEx(key: String, ttl: Duration): Map<String, String> {
        expire(key, ttl.inWholeSeconds)
        return hgetAll(key)
    }


    private fun Jedis.saddex(key: String, ttl: Duration, vararg members: String) {
        sadd(key, *members)
        expire(key, ttl.inWholeSeconds)
    }

    private fun Jedis.smembersex(key: String, ttl: Duration): Set<String> {
        expire(key, ttl.inWholeSeconds)
        return smembers(key)
    }

    private fun keyOfPreviousLevel(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:parents")

    private fun keyOfTreeParent(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:prev")

    private fun keyOfValue(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:val")


    private fun searchAndAddComponent(jedis: Jedis, component: String): CharacterKey {
        if (lastElement == 0L) jedis.set(LAST_KEY, lastElement.toString())

        var currNode = ROOT
        var currNodeChildren = jedis.hgetAll(currNode.key).toMutableMap()
        component.forEach {
            val char = it.toString()
            val maybeIndex = currNodeChildren[char]
            val index = if (maybeIndex != null) maybeIndex else {
                jedis.set(LAST_KEY, lastElement.toString())
                (++lastElement).toString()
            }
            currNodeChildren[char] = index
            jedis.hsetex(currNode.key, ttl, currNodeChildren)

            jedis.setex(keyOfTreeParent(CharacterKey(index)).key, ttl.inWholeSeconds, currNode.key)
            jedis.setex(keyOfValue(CharacterKey(index)).key, ttl.inWholeSeconds, char)

            currNode = CharacterKey(index)
            currNodeChildren = jedis.hgetAll(currNode.key).toMutableMap()
        }

        jedis.expire(currNode.key, ttl.inWholeSeconds)

        return currNode
    }

    private fun findRouteToKey(jedis: Jedis, key: CharacterKey): String? {
        val route = StringBuilder()
        var lastKey = key
        while (lastKey != ROOT) {
            val prevKeyPointer = keyOfTreeParent(lastKey)
            val prevKey = jedis.get(prevKeyPointer.key)
            if (prevKey == null) return null

            jedis.expire(prevKeyPointer.key, ttl.inWholeSeconds)
            jedis.expire(prevKey, ttl.inWholeSeconds)

            val lastKeyChar = jedis.get(keyOfValue(lastKey).key)
            if (lastKeyChar == null) return null
            route.append(lastKeyChar)
            lastKey = CharacterKey(prevKey)
        }
        return route.toString().reversed()
    }

    fun filenameDFS(
        jedis: Jedis,
        key: CharacterKey,
        limit: Int,
        collected: MutableSet<String> = mutableSetOf()
    ): List<String> {
        if (collected.size >= limit) return collected.take(limit)

        val children = jedis.hgetAllEx(key.key, ttl)

        val parentKeys = jedis.smembersex(keyOfPreviousLevel(key).key, ttl)
        parentKeys
            .mapNotNull { parentKey -> findRouteToKey(jedis, CharacterKey(parentKey)) }
            .forEach { if (collected.size < limit) {
                collected.add(it)
            }}

        for ((_, charKey) in children) {
            if (collected.size >= limit) break
            val childKey = CharacterKey(charKey)
            filenameDFS(jedis, childKey, limit, collected)
        }

        return collected.toList()
    }

    private fun insertComponent(jedis: Jedis, component: String, parent: CharacterKey?): CharacterKey {
        val currNode = searchAndAddComponent(jedis, component)
        parent?.let { jedis.saddex(keyOfPreviousLevel(currNode).key, ttl, parent.key) }

        return currNode
    }

    override fun insert(filename: FilenameComponents) = pool.withJedis { jedis ->
        val fullFileKey = insertComponent(jedis, filename.joinToString(File.separator), null)
        insertComponent(jedis, filename.last(), fullFileKey)
        return@withJedis
    }


    override fun lookup(
        filenamePrefix: String,
        limit: Int
    ): List<FilenameComponents> = pool.withJedis { jedis ->
        if (lastElement == -1L) jedis.set(LAST_KEY, lastElement.toString())

        var currNode = ROOT
        var currNodeChildren = jedis.hgetAll(currNode.key)

        filenamePrefix.forEach {
            val char = it.toString()
            val index = currNodeChildren[char]
            if (index == null) return emptyList()
            jedis.expire(currNode.key, ttl.inWholeSeconds)

            jedis.expire(keyOfTreeParent(CharacterKey(index)).key, ttl.inWholeSeconds)

            currNode = CharacterKey(index)
            currNodeChildren = jedis.hgetAll(currNode.key)
        }

        filenameDFS(jedis, currNode, limit).map { it.split(File.separator) }
    }
}