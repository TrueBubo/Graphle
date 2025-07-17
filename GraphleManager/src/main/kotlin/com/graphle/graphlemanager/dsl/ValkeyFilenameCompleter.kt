package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import kotlin.time.Duration.Companion.days

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
    private var lastElement = pool.withJedis { it.get("last") }?.toLong() ?: -1
    private const val ROOT = "0"



    private fun insertComponent(jedis: Jedis, component: String, parent: String?): String {
        if (lastElement == -1L) jedis.set("last", lastElement.toString())

        var currNode = ROOT
        var currNodeChildren = jedis.hgetAll(currNode).toMutableMap()
        component.forEach {
            val char = it.toString()
            val index = currNodeChildren[char] ?: (++lastElement).toString()
            currNodeChildren[char] = index
            jedis.hset(currNode, currNodeChildren)
            jedis.expire(currNode, ttl.inWholeSeconds)

            currNode = index
            currNodeChildren = jedis.hgetAll(currNode).toMutableMap()
        }

        parent?.let { jedis.sadd("$currNode:parents", parent) }
        return currNode;
    }

    override fun insert(filename: FilenameComponents) {
        pool.withJedis { jedis ->
            var parent: String? = null
            filename.forEach { component ->
                parent = insertComponent(jedis, component, parent)
            }
        }
    }

    override fun lookup(
        filenamePrefix: String,
        limit: Int
    ): List<FilenameComponents> {
        TODO("Not yet implemented")
    }
}