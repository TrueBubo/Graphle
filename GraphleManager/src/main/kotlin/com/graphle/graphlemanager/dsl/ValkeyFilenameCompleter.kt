package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import org.springframework.stereotype.Service

@Service
class ValkeyFilenameCompleter(autoCompleteProperties: AutoCompleteProperties) {
    private inline fun <T> JedisPool.withJedis(action: (Jedis) -> T): T {
        return this.resource.use { jedis -> action(jedis) }
    }

    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 10
        maxIdle = 5
        minIdle = 1
        testOnBorrow = true
    }

    private val host = autoCompleteProperties.valkey.host
    private val port = autoCompleteProperties.valkey.port
    private val pool = JedisPool(poolConfig, host, port)

    val filenameCompleter = pool.withJedis { jedis ->
        FilenameCompleter(JedisStorage(jedis))
    }
}