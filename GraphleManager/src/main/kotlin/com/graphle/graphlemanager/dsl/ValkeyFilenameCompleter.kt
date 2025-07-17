package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
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
    private var root = pool.withJedis { it.get("0") }?.let { "0" }

    @OptIn(ExperimentalContracts::class)
    private fun initializeTrie(jedis: Jedis) {
        jedis.set("last", "0")
        root = "0"
        jedis.hset(ValkeyFilenameCompleter.root, mapOf())
        jedis.expire(ValkeyFilenameCompleter.root, ttl.inWholeSeconds)

    }

    private fun insertComponent(jedis: Jedis, component: String, parent: String?): String {
        initializeTrie(jedis)

        var currNode = root!!
        var currNodeChildren = jedis.hgetAll(currNode)
        component.forEach {
            if (jedis.get(currNode) == null) jedis.hset(currNode, mapOf())

            jedis.expire(currNode, ttl.inWholeSeconds)

            val char = it.toString()
            val index = currNodeChildren[char] ?: (++lastElement).toString()
            currNodeChildren[char] = index
            jedis.hset(currNode, currNodeChildren)

            currNode = index
            currNodeChildren = jedis.hgetAll(currNode)
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