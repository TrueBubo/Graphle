package com.graphle.common

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.apache.commons.io.FileUtils
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import kotlin.time.Duration.Companion.milliseconds

/**
 * Application configuration loaded from config.yaml resource file.
 */
val config = Config.load(
    Thread.currentThread().contextClassLoader
        .getResourceAsStream("config.yaml")?.bufferedReader().use { it?.readText() }
        ?: error("Config file config.yaml does not exist, please create it")
).getOrThrow()

/**
 * Supervisor coroutine scope for IO operations that continues despite individual task failures.
 */
val supervisorIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

private val serverURL = "http://localhost:${config.server.port}"
private val graphQlServerURL = "${serverURL}/graphql"

/**
 * Server URL for downloading files.
 */
val downloadURL = "${serverURL}/download"

/**
 * Server URL for DSL command interpretation.
 */
val dslURL = "${serverURL}/dsl"

/**
 * Apollo GraphQL client configured with the server URL.
 */
val apolloClient = ApolloClient.Builder()
    .serverUrl(graphQlServerURL)
    .build()

/**
 * User's home directory path.
 */
val userHome: String = FileUtils.getUserDirectory().path

/**
 * System clipboard for copy/paste operations.
 */
val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
