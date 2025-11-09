package com.graphle.graphlemanager.connection

import com.graphle.graphlemanager.BaseGraphQlIntegrationTest
import com.graphle.graphlemanager.FileTestUtils
import com.graphle.graphlemanager.file.AbsolutePathString
import com.graphle.graphlemanager.sweeper.Neo4JSweeper
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test

class ConnectionIntegrationTests(
    @Autowired private val connectionService: ConnectionService,
    @Autowired private val fileTestUtils: FileTestUtils,
    @Autowired private val neo4JSweeper: Neo4JSweeper
) : BaseGraphQlIntegrationTest() {
    private val randomString = (1..20).map { ('a'..'z').random() }.joinToString("")

    @AfterTest
    fun tearDown() {
        neo4JSweeper.sweep()
    }

    @Test
    fun `insert connection`() = fileTestUtils.withTempFiles(count = 2) { (from, to) ->
        try {
            post { insertConnectionQuery(from = from.absolutePath, to = to.absolutePath) }
        } finally {
            removeConnectionDb(from = from.absolutePath, to = to.absolutePath)
        }
    }


    @Test
    fun `connection was inserted and fetched`() = fileTestUtils.withTempFiles(count = 2) { (from, to) ->
        try {
            post { insertConnectionQuery(from = from.absolutePath, to = to.absolutePath) }
            expectThat(
                fetchListPost<Connection> { fetchConnectionsQuery(from.absolutePath) }
                    .filter { it.name == "tmp_connection_$randomString" }
            )
                .hasSize(1)
                .first()
                .and {
                    get { from }.isEqualTo(from)
                    get { to }.isEqualTo(to)
                }
        } finally {
            removeConnectionDb(from = from.absolutePath, to = to.absolutePath)
        }

    }

    private fun insertConnectionQuery(from: String, to: String) = mutation {
        addConnection(
            ConnectionInput(
                name = "tmp_connection_$randomString",
                value = null,
                from = from,
                to = to,
                bidirectional = true
            )
        )
    }

    private fun fetchConnectionsQuery(location: AbsolutePathString) = query {
        fileByLocation(location)
    }

    private fun removeConnectionDb(from: AbsolutePathString, to: AbsolutePathString) {
        connectionService.removeConnection(
            ConnectionInput(
                name = "tmp_connection_$randomString",
                value = null,
                from = from,
                to = to,
                bidirectional = true
            )
        )
    }

    companion object {
        private fun addConnection(connection: ConnectionInput) = """
          addConnection(connection: {
                name: "${connection.name}",
                ${if (connection.value == null) "" else "value: \"${connection.value}\","} 
                from: "${connection.from}",
                to: "${connection.to}",
                bidirectional: ${connection.bidirectional}
          }) {
            name
            value
            from
            to
            bidirectional
          }
        """.trimIndent()

        private fun fileByLocation(location: AbsolutePathString) = """
            fileByLocation(location: "$location") {
                connections {
                    name
                    value
                    from
                    to
                    bidirectional
                }
            }
        """.trimIndent()

        private fun removeConnection(connection: ConnectionInput) = """
          removeConnection(connection: {
                name: "${connection.name}",
                ${if (connection.value == null) "" else "value: \"${connection.value}\","} 
                locationFrom: "${connection.from}",
                locationTo: "${connection.to}",
                bidirectional: ${connection.bidirectional}
          }) {
            name
            value
            locationFrom
            locationTo
            bidirectional
          }
        """.trimIndent()

    }
}