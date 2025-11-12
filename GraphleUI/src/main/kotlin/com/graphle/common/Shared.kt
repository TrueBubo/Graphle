package com.graphle.common

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.apache.commons.io.FileUtils
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import kotlin.time.Duration.Companion.milliseconds

val config = Config.load(
    Thread.currentThread().contextClassLoader
        .getResourceAsStream("config.yaml")?.bufferedReader().use { it?.readText() }
        ?: error("Config file config.yaml does not exist, please create it")
).getOrThrow()

val supervisorIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

private val serverURL = "http://localhost:${config.server.port}"
private val graphQlServerURL = "${serverURL}/graphql"
val downloadURL = "${serverURL}/download"
val dslURL = "${serverURL}/dsl"
val apolloClient = ApolloClient.Builder()
    .serverUrl(graphQlServerURL)
    .build()

val minUpdateDelay = 1000.milliseconds

val userHome: String = FileUtils.getUserDirectory().path

val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
