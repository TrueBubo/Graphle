package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import java.io.File
import kotlin.text.forEach
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

inline fun <T> JedisPool.withJedis(action: (Jedis) -> T): T {
    return this.resource.use { jedis -> action(jedis) }
}

private val poolConfig = JedisPoolConfig().apply {
    maxTotal = 10
    maxIdle = 5
    minIdle = 1
    testOnBorrow = true
}
private const val HOST = "localhost"
private const val PORT = 6379

private val pool = JedisPool(poolConfig, HOST, PORT)

val valkeyFilenameCompleter = pool.withJedis { jedis ->
    FilenameCompleter(JedisStorage(jedis))
}