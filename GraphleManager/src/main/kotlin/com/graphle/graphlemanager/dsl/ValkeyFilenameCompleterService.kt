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
    /**
     * Extension function to safely execute an action with a Jedis connection from the pool.
     * @param action The action to perform with the Jedis connection
     * @return The result of the action
     */
    private inline fun <T> JedisPool.withJedis(action: (Jedis) -> T): T {
        return this.resource.use { jedis -> action(jedis) }
    }

    /**
     * Configuration for the Jedis connection pool.
     */
    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 10
        maxIdle = 5
        minIdle = 1
        testOnBorrow = true
    }

    /**
     * The Valkey host address from configuration.
     */
    private val host = autoCompleteProperties.valkey.host

    /**
     * The Valkey port number from configuration.
     */
    private val port = autoCompleteProperties.valkey.port

    /**
     * The Jedis connection pool for Valkey database.
     */
    private val pool = JedisPool(poolConfig, host, port)

    /**
     * The filename completer instance backed by Valkey storage.
     */
    override val completer = pool.withJedis { jedis ->
        FilenameCompleter(JedisStorage(jedis))
    }
}