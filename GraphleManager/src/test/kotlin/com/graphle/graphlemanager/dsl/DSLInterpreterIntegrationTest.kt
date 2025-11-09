package com.graphle.graphlemanager.dsl

import com.graphle.graphlemanager.BaseRestIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.connection.ConnectionInput
import com.graphle.graphlemanager.connection.ConnectionService
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.Test

class DSLInterpreterIntegrationTest : BaseRestIntegrationTest() {
    @Autowired
    private lateinit var connectionService: ConnectionService
    @Autowired
    private lateinit var fileTestUtils: FileTestUtils
    private val randomString = (1..20).map { ('a'..'z').random() }.joinToString("")

    @Test
    fun `should run the select command via api`() = fileTestUtils.withTempFiles(3) { (file1, file2, file3) ->
        connectionService.addConnection(
            ConnectionInput(
                name = randomString,
                value = null,
                from = file1.absolutePath,
                to = file2.absolutePath,
                bidirectional = false
            )
        )

        connectionService.addConnection(
            ConnectionInput(
                name = randomString + "2",
                value = null,
                from = file2.absolutePath,
                to = file3.absolutePath,
                bidirectional = false
            )
        )

        expectThat(post<DSLResponse>(url = URL) {
            """
                { "command": "(location = \"$file1\")[name = \"$randomString\"]()[name = \"${randomString}2\"]()[pred]()" }
            """.trimIndent()
        }).isNotNull().and {
            get { type }.isEqualTo(ResponseType.FILENAMES)
            get { responseObject }.hasSize(1).first().isEqualTo((file3.parent))
        }
    }

    companion object {
        const val URL = "/dsl"
    }
}