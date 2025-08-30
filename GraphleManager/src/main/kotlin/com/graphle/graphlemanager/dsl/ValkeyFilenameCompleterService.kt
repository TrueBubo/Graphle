package com.graphle.graphlemanager.dsl

import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import org.springframework.stereotype.Service

/**
 * Service used for creating filename completer utilizing Valkey
 * @param autoCompleteProperties Connection details for database
 */
@Service
class ValkeyFilenameCompleterService(autoCompleteProperties: AutoCompleteProperties): FilenameCompleterService {
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

    // Can be used as completer
    override val completer = pool.withJedis { jedis ->
        FilenameCompleter(JedisStorage(jedis))
    }
}