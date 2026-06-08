package com.graphle.graphlemanager.dsl

import io.valkey.JedisPool
import io.valkey.JedisPoolConfig
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service

/**
 * Service used for creating filename completer utilizing Valkey
 * @param autoCompleteProperties Connection details for database
 */
@Service
class ValkeyFilenameCompleterService(autoCompleteProperties: AutoCompleteProperties): FilenameCompleterService {
    /**
     * Configuration for the Jedis connection pool.
     */
    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 10
        maxIdle = 5
        minIdle = 1
        testOnBorrow = false
        testOnReturn = false
        testWhileIdle = false
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
    override val completer = FilenameCompleter(JedisStorage(pool))

    @PreDestroy
    fun close() {
        pool.close()
    }
}
