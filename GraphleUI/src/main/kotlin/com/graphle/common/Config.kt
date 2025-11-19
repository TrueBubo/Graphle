package com.graphle.common

import com.graphle.common.Server.Companion.parse
import org.yaml.snakeyaml.Yaml
import kotlin.collections.get

/**
 * Application configuration data class.
 *
 * @property server Server configuration containing port and localhost settings
 */
data class Config(
    val server: Server
) {
    companion object {
        /**
         * Loads and parses configuration from YAML text.
         *
         * @param configText YAML configuration text to parse
         * @return Result containing parsed Config or error
         */
        fun load(configText: String): Result<Config> = runCatching {
            val yaml = Yaml()
            val map = yaml.load<Map<String, Any>>(configText)
            val notProcessed = map.toMutableMap()

            val serverMap = (map["server"] as? Map<*, *>) ?: error("Missing 'server' section in config")
            notProcessed.remove("server")
            val server = serverMap.parse()

            val config = Config(
                server = server
            )

            if (notProcessed.isNotEmpty()) error(
                "The configuration has unexpected parameters ${
                    notProcessed.keys.joinToString(separator = " ")
                }"
            )

            return Result.success(config)
        }
    }
}

/**
 * Server configuration data class.
 *
 * @property port Server port number (must be between 1 and 65535)
 * @property localhost Whether the server is running on localhost
 */
data class Server(val port: Int, val localhost: Boolean) {
    companion object {
        /**
         * Parses server configuration from a map.
         *
         * @receiver Map containing server configuration
         * @return Parsed Server instance
         */
        internal fun Map<*, *>.parse(): Server {
            val notProcessed = this.toMutableMap()
            val server = Server(
                port = (this["port"] as? Int ?: error("Missing 'port' in server config"))
                    .also {
                        require(it in 1..65535) { "Port must be between 1 and 65535" }
                        notProcessed.remove("port")
                    },
                localhost = (this["localhost"] as? Boolean ?: error("Missing 'localhost' in server config"))
                    .also {
                        notProcessed.remove("localhost")
                    }
            )
            if (notProcessed.isNotEmpty()) error(
                "The server segment in configuration has unexpected parameters ${
                    notProcessed.keys.joinToString(
                        separator = " "
                    )
                }"
            )

            return server
        }
    }
}