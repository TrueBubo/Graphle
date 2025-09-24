package com.graphle

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.apache.commons.io.FileUtils
import java.awt.Toolkit
import kotlin.time.Duration.Companion.milliseconds

val config = Config.load(
    Thread.currentThread().contextClassLoader
        .getResourceAsStream("config.yaml")?.bufferedReader().use { it?.readText() }
        ?: error("Config file config.yaml does not exist, please create it")
).getOrThrow()

val supervisorIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

private const val serverURL = "http://localhost:8080"
private const val graphQlServerURL = "${serverURL}/graphql"
val downloadURL = "${serverURL}/download"
val apolloClient = ApolloClient.Builder()
    .serverUrl(graphQlServerURL)
    .build()

val minUpdateDelay = 1000.milliseconds

val userHome: String = FileUtils.getUserDirectory().path

val clipboard = Toolkit.getDefaultToolkit().systemClipboard
