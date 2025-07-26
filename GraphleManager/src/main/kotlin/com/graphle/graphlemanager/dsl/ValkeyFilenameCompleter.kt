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
    private const val ROOT_INDEX_KEY = 0L
    private const val TRUE_CHAR = "1"
    private var lastElement = pool.withJedis { it.get(LAST_KEY) }?.toLong() ?: ROOT_INDEX_KEY
    private val ROOT = CharacterKey(ROOT_INDEX_KEY.toString())

    private val childrenCache = ConcurrentCache<String, Map<String, String>>(ttl)
    private val previousLevelCache = ConcurrentCache<String, MutableSet<String>>(ttl)
    private val treeParentCache = ConcurrentCache<String, String>(ttl)
    private val valueCache = ConcurrentCache<String, String>(ttl)
    private val fullPathEndCache = ConcurrentCache<String, String>(ttl)

    private fun Jedis.getEx(key: String, ttl: Duration, onGet: (String) -> String?): String? {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: get(key)
    }

    private fun Jedis.setEx(key: String, ttl: Duration, value: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        set(key, value)
        onSet(key, value)
    }

    private fun Jedis.hsetex(
        key: String,
        ttl: Duration,
        hash: Map<String, String>,
        onSet: (String, Map<String, String>) -> Unit
    ) {
        expire(key, ttl.inWholeSeconds)
        hset(key, hash)
        onSet(key, hash)
    }

    private fun Jedis.hgetAllEx(
        key: String,
        ttl: Duration,
        onGet: (String) -> Map<String, String>?
    ): Map<String, String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: hgetAll(key)
    }


    private fun Jedis.saddex(key: String, ttl: Duration, member: String, onSet: (String, String) -> Unit) {
        expire(key, ttl.inWholeSeconds)
        sadd(key, member)
        onSet(key, member)
    }

    private fun Jedis.smembersex(key: String, ttl: Duration, onGet: (String) -> Set<String>?): Set<String> {
        expire(key, ttl.inWholeSeconds)
        val cachedValue = onGet(key)
        return cachedValue ?: smembers(key)
    }

    private fun keyOfPreviousLevel(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:parents")

    private fun keyOfTreeParent(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:prev")

    private fun keyOfValue(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:val")

    private fun keyOfFullPathEnd(key: CharacterKey): CharacterKey = CharacterKey("${key.key}:full")


    private fun searchAndAddComponent(jedis: Jedis, component: String): CharacterKey {
        if (lastElement == ROOT_INDEX_KEY) jedis.set(LAST_KEY, ROOT_INDEX_KEY.toString())

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
            jedis.hsetex(currNode.key, ttl, currNodeChildren) { key, value -> childrenCache[key] = value }

            jedis.setEx(
                keyOfTreeParent(CharacterKey(index)).key,
                ttl,
                currNode.key
            ) { key, value -> treeParentCache[key] = value }
            jedis.setEx(keyOfValue(CharacterKey(index)).key, ttl, char) { key, value -> valueCache[key] = value }

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

        val children = jedis.hgetAllEx(key.key, ttl) { childrenCache[it] }

        if (collected.size < limit && jedis.getEx(
                keyOfFullPathEnd(key).key,
                ttl
            ) { fullPathEndCache[it] } == TRUE_CHAR
        ) {
            findRouteToKey(jedis, key)?.let(collected::add)
        }
        val previousLevelKeys = jedis.smembersex(keyOfPreviousLevel(key).key, ttl) { previousLevelCache[it] }
        previousLevelKeys
            .mapNotNull { parentKey -> findRouteToKey(jedis, CharacterKey(parentKey)) }
            .filter { it !in collected }
            .forEach(collected::add)


        for ((_, charKey) in children) {
            if (collected.size >= limit) break
            val childKey = CharacterKey(charKey)
            filenameDFS(jedis, childKey, limit, collected)
        }

        return collected.toList()
    }

    private fun insertComponent(jedis: Jedis, component: String, parent: CharacterKey?): CharacterKey {
        val currNode = searchAndAddComponent(jedis, component)
        if (parent == null) {
            jedis.setEx(keyOfFullPathEnd(currNode).key, ttl, TRUE_CHAR) { key, value -> fullPathEndCache[key] = value }
        }
        parent?.let {
            val previousLevelKey = keyOfPreviousLevel(currNode).key
            jedis.saddex(previousLevelKey, ttl, parent.key) { key, value ->
                if (previousLevelCache[key] == null) previousLevelCache[key] = mutableSetOf()
                previousLevelCache[key]?.add(value)
            }
        }

        return currNode
    }

    override fun insert(filename: FilenameComponents) = pool.withJedis { jedis ->
        val fullFileKey =
            insertComponent(jedis, filename.joinToString(prefix = File.separator, separator = File.separator), null)
        insertComponent(jedis, filename.last(), fullFileKey)
        return@withJedis
    }


    override fun lookup(
        filenamePrefix: String,
        limit: Int
    ): List<FilenameComponents> = pool.withJedis { jedis ->
        if (lastElement == ROOT_INDEX_KEY) jedis.set(LAST_KEY, ROOT_INDEX_KEY.toString())

        var currNode = ROOT
        var currNodeChildren = jedis.hgetAllEx(currNode.key, ttl) { childrenCache[it] }

        filenamePrefix.forEach { character ->
            val char = character.toString()
            val index = currNodeChildren[char]
            if (index == null) return emptyList()

            jedis.expire(keyOfTreeParent(CharacterKey(index)).key, ttl.inWholeSeconds)

            currNode = CharacterKey(index)
            currNodeChildren = jedis.hgetAllEx(currNode.key, ttl) { childrenCache[it] }
        }

        filenameDFS(jedis, currNode, limit).map { it.split(File.separator) }
    }
}