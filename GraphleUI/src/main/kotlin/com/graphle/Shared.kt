package com.graphle

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.apache.commons.io.FileUtils
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import kotlin.time.Duration.Companion.milliseconds


val supervisorIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

private const val serverURL = "http://localhost:8080/graphql"
val apolloClient = ApolloClient.Builder()
    .serverUrl(serverURL)
    .build()

val minUpdateDelay = 1000.milliseconds

val userHome: String = FileUtils.getUserDirectory().path

val clipboard = Toolkit.getDefaultToolkit().systemClipboard